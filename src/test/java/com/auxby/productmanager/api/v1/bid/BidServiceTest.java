package com.auxby.productmanager.api.v1.bid;

import com.auxby.productmanager.api.v1.bid.model.PlaceBidRequest;
import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.bid.repository.BidRepository;
import com.auxby.productmanager.api.v1.offer.OfferService;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.user.UserService;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.exception.ActionNotAllowException;
import com.auxby.productmanager.exception.BidDeclinedException;
import com.auxby.productmanager.exception.InsufficientCoinsException;
import com.auxby.productmanager.rabbitmq.MessageSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @InjectMocks
    private BidService service;
    @Mock
    private UserService userService;
    @Mock
    private OfferService offerService;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private MessageSender messageSender;

    @Test
    void should_AcceptBid_When_RequestIsValidAndOfferCurrencyIsEuro() {
        //Arrange
        var mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("joe.doe@gmail.com");
        when(mockUser.getAvailableCoins()).thenReturn(100);

        var mockOwner = mock(UserDetails.class);
        when(mockOwner.getUsername()).thenReturn("sam.smith@gmail.com");

        var mockBidOwner = mock(UserDetails.class);
        when(mockBidOwner.getUsername()).thenReturn("andreea.long@gmail.com");
        when(mockBidOwner.getId()).thenReturn(999005320);

        var mockBid = mock(Bid.class);
        when(mockBid.getOwner()).thenReturn(mockBidOwner);
        when(mockBid.getBidValue()).thenReturn(BigDecimal.valueOf(2000));

        var mockOffer = mock(Offer.class);
        when(mockOffer.getAuctionEndDate())
                .thenReturn(new Date(System.currentTimeMillis() + 10000));
        when(mockOffer.getOwner()).thenReturn(mockOwner);
        when(mockOffer.getName()).thenReturn("UnitTest");
        when(mockOffer.getBids()).thenReturn(Set.of(mockBid));
        var price = BigDecimal.valueOf(2300);

        var request = new PlaceBidRequest(1, BigDecimal.valueOf(2500), 20);
        when(userService.getUser()).thenReturn(Optional.of(mockUser));
        when(offerService.findAvailableOnAuctionOfferById(anyInt())).thenReturn(mockOffer);
        when(offerService.computePrice(any())).thenReturn(price);

        //Act
        var result = service.createBidForOnAuctionOffer(request);

        //Asserts
        ArgumentCaptor<Bid> bidArg = ArgumentCaptor.forClass(Bid.class);
        assertAll(
                () -> assertTrue(result.wasBidAccepted()),
                () -> verify(mockOffer, times(1)).addBid(bidArg.capture()),
                () -> verify(messageSender, times(1)).send(any()),
                () -> verify(userService, times(1)).updateUserCoinsNumber(anyString(), anyInt()),
                () -> assertEquals(BigDecimal.valueOf(2500), bidArg.getValue().getBidValue()),
                () -> assertEquals("joe.doe@gmail.com", bidArg.getValue().getBidderName())
        );
    }

    @Test
    void should_AcceptBid_When_RequestIsValidAndOfferCurrencyIsRon() {
        //Arrange
        var mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("joe.doe@gmail.com");
        when(mockUser.getAvailableCoins()).thenReturn(100);

        var mockOwner = mock(UserDetails.class);
        when(mockOwner.getUsername()).thenReturn("sam.smith@gmail.com");

        var mockOffer = mock(Offer.class);
        when(mockOffer.getAuctionEndDate())
                .thenReturn(new Date(System.currentTimeMillis() + 100000));
        when(mockOffer.getBids()).thenReturn(new HashSet<>());
        when(mockOffer.getOwner()).thenReturn(mockOwner);

        var request = new PlaceBidRequest(1, BigDecimal.valueOf(2000), 20);
        when(userService.getUser()).thenReturn(Optional.of(mockUser));
        when(offerService.findAvailableOnAuctionOfferById(anyInt())).thenReturn(mockOffer);
        when(offerService.computePrice(any())).thenReturn(BigDecimal.valueOf(2000));

        //Act
        var result = service.createBidForOnAuctionOffer(request);

        //Asserts
        ArgumentCaptor<Bid> bidArg = ArgumentCaptor.forClass(Bid.class);
        assertAll(
                () -> assertTrue(result.wasBidAccepted()),
                () -> verify(mockOffer, times(1)).addBid(bidArg.capture()),
                () -> verify(userService, times(1)).updateUserCoinsNumber(anyString(), anyInt()),
                () -> assertEquals(BigDecimal.valueOf(2000), bidArg.getValue().getBidValue()),
                () -> assertEquals("joe.doe@gmail.com", bidArg.getValue().getBidderName())
        );
    }

    @Test
    void should_DeclineBid_When_AuctionEnded() {
        //Arrange
        var request = new PlaceBidRequest(1, BigDecimal.valueOf(2000), 20);
        var mockUser = mock(UserDetails.class);
        var mockOffer = mock(Offer.class);
        when(mockOffer.getAuctionEndDate())
                .thenReturn(new Date(System.currentTimeMillis() - 100000));

        when(userService.getUser()).thenReturn(Optional.of(mockUser));
        when(offerService.findAvailableOnAuctionOfferById(anyInt())).thenReturn(mockOffer);

        //Act && Assert
        assertThrows(ActionNotAllowException.class, () -> service.createBidForOnAuctionOffer(request));
        assertAll(
                () -> verify(userService, times(0)).updateUserCoinsNumber(anyString(), anyInt()),
                () -> verify(mockOffer, times(0)).addBid(any())
        );
    }

    @Test
    void should_DeclineBid_When_MinAuctionValueIsNotExceeded() {
        //Arrange
        var mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("joe.doe@gmail.com");

        var mockOwner = mock(UserDetails.class);
        when(mockOwner.getUsername()).thenReturn("sam.smith@gmail.com");

        var mockOffer = mock(Offer.class);
        when(mockOffer.getAuctionEndDate())
                .thenReturn(new Date(System.currentTimeMillis() + 100000));
        when(mockOffer.getBids()).thenReturn(new HashSet<>());
        when(mockOffer.getOwner()).thenReturn(mockOwner);

        var request = new PlaceBidRequest(1, BigDecimal.valueOf(1999), 20);
        when(userService.getUser()).thenReturn(Optional.of(mockUser));
        when(offerService.findAvailableOnAuctionOfferById(anyInt())).thenReturn(mockOffer);
        when(offerService.computePrice(any())).thenReturn(BigDecimal.valueOf(2000));

        //Act && Assert
        assertThrows(ActionNotAllowException.class, () -> service.createBidForOnAuctionOffer(request));
        assertAll(
                () -> verify(userService, times(0)).updateUserCoinsNumber(anyString(), anyInt()),
                () -> verify(mockOffer, times(0)).addBid(any())
        );
    }

    @Test
    void should_DeclineBid_When_HighestBidValueIsNotExceeded() {
        //Arrange
        var mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("joe.doe@gmail.com");

        var mockOwner = mock(UserDetails.class);
        when(mockOwner.getUsername()).thenReturn("sam.smith@gmail.com");

        var mockBidOwner = mock(UserDetails.class);
        when(mockBidOwner.getUsername()).thenReturn("andreea.long@gmail.com");

        var mockBid = mock(Bid.class);
        when(mockBid.getOwner()).thenReturn(mockBidOwner);
        when(mockBid.getBidValue()).thenReturn(BigDecimal.valueOf(2350));

        var mockOffer = mock(Offer.class);
        when(mockOffer.getAuctionEndDate())
                .thenReturn(new Date(System.currentTimeMillis() + 100000));
        when(mockOffer.getBids()).thenReturn(Set.of(mockBid));
        when(mockOffer.getOwner()).thenReturn(mockOwner);

        var request = new PlaceBidRequest(1, BigDecimal.valueOf(2300), 20);
        when(userService.getUser()).thenReturn(Optional.of(mockUser));
        when(offerService.findAvailableOnAuctionOfferById(anyInt())).thenReturn(mockOffer);
        when(offerService.computePrice(any())).thenReturn(BigDecimal.valueOf(2000));

        //Act && Assert
        assertThrows(BidDeclinedException.class, () -> service.createBidForOnAuctionOffer(request));
        assertAll(
                () -> verify(userService, times(0)).updateUserCoinsNumber(anyString(), anyInt()),
                () -> verify(mockOffer, times(0)).addBid(any())
        );
    }

    @Test
    void should_DeclineBid_When_UserHasNotEnoughCoins() {
        //Arrange
        var mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("joe.doe@gmail.com");
        when(mockUser.getAvailableCoins()).thenReturn(0);

        var mockOwner = mock(UserDetails.class);
        when(mockOwner.getUsername()).thenReturn("sam.smith@gmail.com");

        var mockBid = mock(Bid.class);
        when(mockBid.getBidValue()).thenReturn(BigDecimal.valueOf(2250));

        var mockOffer = mock(Offer.class);
        when(mockOffer.getAuctionEndDate())
                .thenReturn(new Date(System.currentTimeMillis() + 100000));
        when(mockOffer.getBids()).thenReturn(Set.of(mockBid));
        when(mockOffer.getOwner()).thenReturn(mockOwner);

        var price = BigDecimal.valueOf(2300);
        var request = new PlaceBidRequest(1, price, 20);
        when(userService.getUser()).thenReturn(Optional.of(mockUser));
        when(offerService.findAvailableOnAuctionOfferById(anyInt())).thenReturn(mockOffer);
        when(offerService.computePrice(any())).thenReturn(price);

        //Act && Assert
        assertThrows(InsufficientCoinsException.class, () -> service.createBidForOnAuctionOffer(request));
        assertAll(
                () -> verify(userService, times(0)).updateUserCoinsNumber(anyString(), anyInt()),
                () -> verify(mockOffer, times(0)).addBid(any())
        );
    }


    @Test
    void should_ReturnEmptyListOfUserBids_When_UserHaveNoBidPlaced() {
        //Arrange
        var mockUser = mock(UserDetails.class);
        when(mockUser.getId()).thenReturn(4500434);
        when(mockUser.getUsername()).thenReturn("leo.dicaprio@gmail.com");
        when(userService.getUser()).thenReturn(Optional.of(mockUser));
        when(userService.getUserFavoriteOffersIds(anyString())).thenReturn(List.of());
        when(bidRepository.getUserBids(anyInt())).thenReturn(List.of());

        //Act
        var result = service.getUserBids();

        //Assert
        assertEquals(0, result.size());
    }
}