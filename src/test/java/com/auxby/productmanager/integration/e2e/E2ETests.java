package com.auxby.productmanager.integration.e2e;

import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.bid.repository.BidRepository;
import com.auxby.productmanager.api.v1.offer.OfferController;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.offer.repository.OfferRepository;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearchCriteria;
import com.auxby.productmanager.api.v1.user.repository.User;
import com.auxby.productmanager.config.security.Role;
import com.auxby.productmanager.integration.TestContainerBase;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class E2ETests extends TestContainerBase {

    @Autowired
    OfferController offerController;
    @Autowired
    OfferRepository offerRepository;
    @Autowired
    BidRepository bidRepository;

    @Test
    @Order(1)
    void assert_AllScheduledAction_PerformedTasks() {
        //Arrange
        List<String> availableOfferNames = List.of("Vintage Watch", "Antique Vase", "Art Print", "PS3", "Vintage Typewriter");

        //Asserts for offers
        var offers = offerRepository.findAll();
        var availableOffers = offers.stream().filter(Offer::isAvailable).toList();
        assertAll(
                () -> assertEquals(9, offers.size()),
                () -> assertEquals(5, availableOffers.size()),
                () -> assertTrue(availableOffers.stream().map(Offer::getName).toList().containsAll(availableOfferNames))
        );

        //Assert bids
        var bids = bidRepository.findAll();
        var winners = bids.stream().filter(Bid::getIsWinner).toList();
        assertAll(
                () -> assertEquals(3, bids.size()),
                () -> assertEquals(2, winners.size())
        );
    }

    @Test
    @Order(2)
    void should_ReturnAvailableOffersInPlatform_When_EndpointMethodCalled() {
        // Act
        var allOffers = offerController.getAllProducts(Pageable.unpaged(), new OfferSearchCriteria());
        var allOffersOnAuction = offerController.getAllProducts(Pageable.unpaged(), OfferSearchCriteria.builder()
                .isOnAuction(true)
                .categories(List.of())
                .build());
        var vintageOffers = offerController.getAllProducts(Pageable.unpaged(), OfferSearchCriteria.builder()
                .title("Vintage")
                .categories(List.of())
                .build());
        var userOffers = offerController.getAllProducts(Pageable.unpaged(), OfferSearchCriteria.builder()
                .userName("joe.doe@gmail.com")
                .build());

        //Assert
        assertAll(
                () -> assertEquals(5, allOffers.getSize()),
                () -> assertEquals(2, vintageOffers.getSize()),
                () -> assertEquals(2, allOffersOnAuction.getSize()),
                () -> assertEquals(2, userOffers.getSize())
        );
    }

    @Test
    @Order(3)
    void should_UploadImageAndGenerateDeepLink_When_EndpointMethodCalled() {

        User user = new User(1, "joe.doe@gmail.com", "pass4Test.", "joe.doe@gmail.com", Role.USER, true);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

        //Arrange
        var fileMock = mock(MultipartFile.class);
        var offers = offerRepository.findByOwner_Username("joe.doe@gmail.com");
        assertFalse(offers.isEmpty());
        var offer = offers.get(0);

        // Act
        offerController.uploadOfferImages(new MultipartFile[]{fileMock}, Math.toIntExact(offer.getId()));

        //Assert
        var result = offerController.getProductId(1, false);
        assertFalse(result.photos().isEmpty());
        assertTrue(result.deepLink().contains("http://localhost:8080/deepLink"));
    }
}
