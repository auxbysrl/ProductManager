package com.auxby.productmanager.api.v1.offer;

import com.auxby.productmanager.api.v1.category.CategoryService;
import com.auxby.productmanager.api.v1.favorite.FavoriteService;
import com.auxby.productmanager.api.v1.notification.NotificationService;
import com.auxby.productmanager.api.v1.offer.repository.OfferRepository;
import com.auxby.productmanager.api.v1.user.UserService;
import com.auxby.productmanager.api.v1.user.repository.User;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.config.cache.CacheConfiguration;
import com.auxby.productmanager.config.cache.CacheUtils;
import com.auxby.productmanager.config.properties.WebClientProps;
import com.auxby.productmanager.config.security.Role;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.rabbitmq.MessageSender;
import com.auxby.productmanager.utils.enums.ConditionType;
import com.auxby.productmanager.utils.enums.CurrencyType;
import com.auxby.productmanager.utils.mapper.OfferMapper;
import com.auxby.productmanager.utils.mapper.OfferMapperImpl;
import com.auxby.productmanager.utils.service.AmazonClientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = {OfferService.class, CacheUtils.class, OfferMapperImpl.class, CacheConfiguration.class})
public class OfferServiceCacheTest {
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private UserService userService;
    @MockBean
    private WebClientProps webClientProps;
    @MockBean
    private AmazonClientService awsService;
    @MockBean
    private OfferRepository offerRepository;
    @MockBean
    private MessageSender messageSender;
    @MockBean
    private FavoriteService favoriteService;
    @MockBean
    private NotificationService notificationService;
    @Autowired
    private CacheUtils cacheUtils;
    @Spy
    private OfferMapper offerMapper = Mappers.getMapper(OfferMapper.class);
    @Autowired
    private OfferService offerService;

    @BeforeAll
    public static void mockApplicationUser() {
        User user = new User(1, "joe.doe@gmail.com", "pass4Test.", "joe.doe@gmail.com", Role.USER, true);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
    }

    @Test
    public void should_ReturnDBAndCacheData_When_MethodIsCalled() {
        //Arrange
        var mockOffer = mockOffer();
        when(offerRepository.findPromotedOffersExcluding(anySet())).thenReturn(new ArrayList<>(Collections.nCopies(5, mockOffer)));
        when(userService.getUserFavoriteOffersIds(anyString())).thenReturn(List.of());

        //Act
        var firstResult = offerService.getPromotedOffers();
        var secondResult = offerService.getPromotedOffers();
        var thirdResult = offerService.getPromotedOffers();
        var cachedIds = cacheUtils.getCachedOfferIds();

        //Assert
        assertAll(
                () -> assertEquals(firstResult.size(), secondResult.size()),
                () -> assertEquals(secondResult.size(), thirdResult.size()),
                () -> assertEquals(thirdResult.size(), firstResult.size()),
                () -> assertEquals(1, cachedIds.size()),
                () -> assertTrue(cachedIds.contains(1)),
                () -> verify(offerRepository, times(1)).findPromotedOffersExcluding(anySet())
        );
    }


    private Offer mockOffer() {
        Offer offer = new Offer();
        offer.setId(1);
        offer.setPrice(BigDecimal.valueOf(100));
        offer.setName("Test Item");
        offer.setDescription("This is a test.");
        offer.setCondition(ConditionType.NEW);
        offer.setCategoryId(1L);
        offer.setOnAuction(false);
        offer.setOwner(mockUser(1, "test-uuid"));
        offer.setAvailable(true);
        offer.setCurrencyType(CurrencyType.RON.name());
        offer.setPublishDate(new Date());
        offer.setViewsNumber(0);

        return offer;
    }

    private UserDetails mockUser(Integer id, String uuid) {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(id);
        userDetails.setLastName("Doe");
        userDetails.setUsername("Test");
        userDetails.setFirstName("Joe");
        userDetails.setGender("unknown");
        userDetails.setUsername(uuid);
        userDetails.setAvailableCoins(2000);

        return userDetails;
    }
}
