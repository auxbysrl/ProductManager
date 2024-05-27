package com.auxby.productmanager.scheduler;

import com.auxby.productmanager.api.v1.bid.BidRepository;
import com.auxby.productmanager.api.v1.offer.repository.OfferRepository;
import com.auxby.productmanager.api.v1.user.UserDetailsRepository;
import com.auxby.productmanager.entity.Bid;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.rabbitmq.MessageSender;
import com.auxby.productmanager.utils.service.AmazonClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.SystemUtils.USER_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferSchedulerTest {
    @InjectMocks
    private OfferScheduler scheduler;
    @Mock
    private MessageSender messageSender;
    @Mock
    private OfferRepository offerRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private AmazonClientService clientService;
    @Mock
    private UserDetailsRepository userDetailsRepository;

    @Test
    void should_DeleteNothing_When_NoExpiredOffersFound() {
        //Arrange
        when(offerRepository.findAllExpiredOffers())
                .thenReturn(Collections.emptyList());

        //Act
        scheduler.deleteOffersTask();

        //Assert
        verify(offerRepository, times(0))
                .deleteById(anyInt());
        verify(clientService, times(0))
                .deleteOfferResources(anyString(), anyInt());
    }

    @Test
    void should_DeleteOffers_When_TaskIsExecuted() {
        //Arrange
        var mockOffer = mock(Offer.class);
        var mockOfferUser = mock(UserDetails.class);
        when(mockOffer.getOwner())
                .thenReturn(mockOfferUser);
        when(mockOffer.getId())
                .thenReturn(1);
        when(mockOfferUser.getUsername())
                .thenReturn("uuid-test");
        when(offerRepository.findAllExpiredOffers())
                .thenReturn(List.of(mockOffer));

        //Act
        scheduler.deleteOffersTask();

        //Assert
        verify(offerRepository, times(1))
                .delete(any());
        ArgumentCaptor<Integer> deleteAwsOfferResourceIdArg = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> deleteAwsOfferResourceUuidArg = ArgumentCaptor.forClass(String.class);
        verify(clientService, times(1))
                .deleteOfferResources(deleteAwsOfferResourceUuidArg.capture(), deleteAwsOfferResourceIdArg.capture());
        assertEquals(1, deleteAwsOfferResourceIdArg.getValue());
        assertEquals("uuid-test", deleteAwsOfferResourceUuidArg.getValue());
    }

    @Test
    void should_CallTheRepositoryMethod_When_TaskIsExecuted() {
        //Act
        scheduler.updateOffersTask();

        //Assert
        verify(offerRepository, times(1)).updateExpiredOffers();
    }

    @Test
    void should_AutoExtendOffers_When_TaskIsCalled() {
        //Arrange
        var userWithCoins = mock(UserDetails.class);
        when(userWithCoins.getAvailableCoins()).thenReturn(5000);
        when(userWithCoins.getUsername()).thenReturn(USER_NAME);

        var userWithoutCoins = mock(UserDetails.class);
        when(userWithoutCoins.getAvailableCoins()).thenReturn(0);

        var mockOfferToExtend = mock(Offer.class);
        when(mockOfferToExtend.getOwner()).thenReturn(userWithCoins);
        when(mockOfferToExtend.getCoinsToExtend()).thenReturn(100);

        var mockOfferToIgnore = mock(Offer.class);
        when(mockOfferToIgnore.getId()).thenReturn(1001);
        when(mockOfferToIgnore.getOwner()).thenReturn(userWithoutCoins);
        when(mockOfferToIgnore.getCoinsToExtend()).thenReturn(100);

        when(offerRepository.getOffersToAutoExtend()).thenReturn(List.of(mockOfferToExtend, mockOfferToIgnore));

        //Act
        scheduler.autoExtendOffersTask();

        //Assert
        ArgumentCaptor<String> userNameArg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> coinsArg = ArgumentCaptor.forClass(Integer.class);
        assertAll(
                () -> verify(mockOfferToExtend, times(1)).setPublishDate(any(Date.class)),
                () -> verify(mockOfferToIgnore, times(1)).setAutoExtend(false),
                () -> verify(userDetailsRepository, times(1)).updateUserCoinsNumber(userNameArg.capture(), coinsArg.capture()),
                () -> verify(mockOfferToExtend, times(1)).setExpirationDate(any(Date.class)),
                () -> verify(messageSender, times(1)).send(any()),
                () -> assertEquals(USER_NAME, userNameArg.getValue()),
                () -> assertEquals(4900, coinsArg.getValue())
        );
    }

    @Test
    void should_setAuctionWinners_When_TaskIsCalled() {
        //Arrange
        var bid1 = new Bid();
        bid1.setBidValue(BigDecimal.valueOf(550));

        var bid2 = new Bid();
        bid2.setBidValue(BigDecimal.valueOf(750));
        var winner = mock(UserDetails.class);
        bid2.setOwner(winner);

        var mockOffer = new Offer();
        mockOffer.setOnAuction(true);
        mockOffer.setBids(Set.of(bid1, bid2));
        when(winner.getId()).thenReturn(10);
        when(offerRepository.getAuctionsToClose()).thenReturn(List.of(mockOffer));

        //Act
        scheduler.setAuctionWinners();

        //Assert
        ArgumentCaptor<Set<Bid>> bidsArg = ArgumentCaptor.forClass(Set.class);
        assertAll(
                () -> verify(bidRepository, times(1)).saveAll(bidsArg.capture()),
                () -> assertEquals(2, bidsArg.getValue().size())
        );
    }
}