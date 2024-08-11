package com.auxby.productmanager.api.v1.offer;

import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.category.CategoryService;
import com.auxby.productmanager.api.v1.commun.entity.Address;
import com.auxby.productmanager.api.v1.commun.entity.Contact;
import com.auxby.productmanager.api.v1.commun.entity.File;
import com.auxby.productmanager.api.v1.commun.system_configuration.SystemConfiguration;
import com.auxby.productmanager.api.v1.commun.system_configuration.SystemConfigurationService;
import com.auxby.productmanager.api.v1.favorite.FavoriteService;
import com.auxby.productmanager.api.v1.notification.NotificationService;
import com.auxby.productmanager.api.v1.offer.model.*;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.offer.repository.OfferRepository;
import com.auxby.productmanager.api.v1.offer.specification.AdvancedOfferSpecification;
import com.auxby.productmanager.api.v1.offer.specification.OfferSpecification;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearch;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearchCriteria;
import com.auxby.productmanager.api.v1.offer.specification.criteria.filter.PriceFilter;
import com.auxby.productmanager.api.v1.user.UserService;
import com.auxby.productmanager.api.v1.user.repository.User;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.config.cache.CacheUtils;
import com.auxby.productmanager.config.security.Role;
import com.auxby.productmanager.exception.ActionNotAllowException;
import com.auxby.productmanager.exception.InsufficientCoinsException;
import com.auxby.productmanager.exception.PhotoUploadException;
import com.auxby.productmanager.rabbitmq.MessageSender;
import com.auxby.productmanager.rabbitmq.message.MessagePayload;
import com.auxby.productmanager.rabbitmq.message.MessageType;
import com.auxby.productmanager.utils.enums.*;
import com.auxby.productmanager.utils.mapper.OfferMapper;
import com.auxby.productmanager.utils.service.AmazonClientService;
import com.auxby.productmanager.utils.service.BranchIOService;
import lombok.SneakyThrows;
import mock.OfferTestMock;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {
    @InjectMocks
    private OfferService offerService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    @Mock
    private AmazonClientService awsService;
    @Mock
    private OfferRepository offerRepository;
    @Mock
    private MessageSender messageSender;
    @Mock
    private FavoriteService favoriteService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CacheUtils cacheUtils;
    @Mock
    private SystemConfigurationService sysConfiguration;
    @Mock
    private BranchIOService branchIOService;
    @Spy
    private OfferMapper offerMapper = Mappers.getMapper(OfferMapper.class);

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
    void getAllOffers() {
        when(offerRepository.findAll(any(OfferSpecification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.nCopies(1, mockOffer(true, true, true))));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        //Act
        var result = offerService.getAllOffers(PageRequest.of(0, 10), new OfferSearchCriteria());

        //Assert
        ArgumentCaptor<Pageable> pageArg = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<OfferSpecification> specArg = ArgumentCaptor.forClass(OfferSpecification.class);
        verify(offerRepository, times(1))
                .findAll(specArg.capture(), pageArg.capture());
        assertEquals(1, result.getContent().size());
        assertEquals(10, pageArg.getValue().getPageSize());
        assertEquals(0, pageArg.getValue().getPageNumber());
    }

    @Test
    void getOffersByIds() {
        when(offerRepository.findAllByIdIn(anyList()))
                .thenReturn(Collections.nCopies(1, mockOffer(true, false, true)));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        var result = offerService.getOffersByIds(List.of(1, 2));
        ArgumentCaptor<List<Integer>> idsArg = ArgumentCaptor.forClass(List.class);
        verify(offerRepository, times(1))
                .findAllByIdIn(idsArg.capture());
        assertEquals(1, result.size());
        assertEquals(2, idsArg.getValue().size());
    }

    @Test
    void getAllUserOffers() {
        when(offerRepository.findByOwner_Username(anyString()))
                .thenReturn(Collections.nCopies(1, mockOffer(false, true, false)));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        var result = offerService.getAllUserOffers("uuid");
        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        verify(offerRepository, times(1))
                .findByOwner_Username(uuidArg.capture());
        assertEquals(1, result.size());
        assertEquals("uuid", uuidArg.getValue());
    }

    @Test
    void getOfferById() {
        var mockOffer = mockOffer(true, true, true);
        when(offerRepository.findOfferById(anyInt()))
                .thenReturn(Optional.of(mockOffer));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        var result = offerService.getOfferById(1, false);
        ArgumentCaptor<Integer> idArg = ArgumentCaptor.forClass(Integer.class);
        verify(offerRepository, times(1))
                .findOfferById(idArg.capture());
        verify(notificationService, times(1))
                .deleteOfferNotifications(anyInt());
        assertEquals(1, idArg.getValue());
        assertEquals(mockOffer.getId(), idArg.getValue());
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(mockOffer.getPrice(), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());
        assertEquals(mockOffer.getAddresses().stream().findFirst().get().getCity(), result.location());
    }

    @Test
    void should_ReturnOfferAndIncreaseView_When_MethodIsCalled() {
        //Arrange
        var mockOffer = mockOffer(true, true, true);
        when(offerRepository.findOfferById(anyInt()))
                .thenReturn(Optional.of(mockOffer));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        //Act
        var result = offerService.getOfferById(1, true);

        //Assert
        ArgumentCaptor<Integer> idArg = ArgumentCaptor.forClass(Integer.class);
        verify(offerRepository, times(1))
                .findOfferById(idArg.capture());
        verify(notificationService, times(1))
                .deleteOfferNotifications(anyInt());
        assertEquals(1, idArg.getValue());
        assertEquals(mockOffer.getId(), idArg.getValue());
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(mockOffer.getPrice(), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());
        assertEquals(mockOffer.getAddresses().stream().findFirst().get().getCity(), result.location());
    }

    @Test
    void getOfferByIdThrowsEntityNotFound() {
        when(offerRepository.findOfferById(anyInt()))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> offerService.getOfferById(1, false));
    }

    @Test
    void createOffer() {
        var mockOffer = mockOffer(true, true, true);
        UserDetails mockUser = mockUser(1, "test-uuid");
        when(userService.getUser())
                .thenReturn(Optional.of(mockUser));
        when(offerRepository.save(any()))
                .thenReturn(mockOffer);
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        var mockContact = new ContactInfo("0750200100", "testLocation");
        var mockOfferInfo = new OfferInfo("Test", "", ConditionType.USED, 1L,
                OfferType.FIXED_PRICE, BigDecimal.valueOf(5000), CurrencyType.RON,
                new Timestamp(System.currentTimeMillis()), mockContact, List.of(), 50, false, "");
        var result = offerService.createOffer(mockOfferInfo);
        assertNotNull(result);
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(mockOffer.getPrice(), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());

        assertCoinsChargeActionIsPerformed(mockUser);
        ArgumentCaptor<Offer> saveOfferArg = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository, times(1)).save(saveOfferArg.capture());
        assertNotNull(saveOfferArg.getValue().getExpirationDate());
        assertNotNull(saveOfferArg.getValue().getPublishDate());
        assertTrue(saveOfferArg.getValue().getPublishDate().before(saveOfferArg.getValue().getExpirationDate()));
    }

    @Test
    void createOfferWithDetails() {
        var mockOffer = mockOffer(true, true, true);
        UserDetails mockUser = mockUser(1, "test-uuid");
        when(userService.getUser())
                .thenReturn(Optional.of(mockUser));
        when(offerRepository.save(any()))
                .thenReturn(mockOffer);
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("5")
                .build());
        var mockContact = new ContactInfo("0750200100", "testLocation");
        BigDecimal postPrice = BigDecimal.valueOf(8000);
        var mockOfferInfo = new OfferInfo("BMW", "This is a used car.", ConditionType.USED, 1L,
                OfferType.FIXED_PRICE, postPrice, CurrencyType.EURO,
                new Timestamp(System.currentTimeMillis()), mockContact, mockListOfDetailsForACar("177"), 50, false, "");
        var result = offerService.createOffer(mockOfferInfo);
        assertNotNull(result);
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(BigDecimal.valueOf(20).setScale(4, RoundingMode.UP), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());

        assertCoinsChargeActionIsPerformed(mockUser);
        ArgumentCaptor<Offer> saveOfferArg = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository, times(1)).save(saveOfferArg.capture());
        assertNotNull(saveOfferArg.getValue().getExpirationDate());
        assertNotNull(saveOfferArg.getValue().getPublishDate());
        assertTrue(saveOfferArg.getValue().getPublishDate().before(saveOfferArg.getValue().getExpirationDate()));
        assertEquals(postPrice.multiply(BigDecimal.valueOf(5).setScale(4, RoundingMode.UP)), saveOfferArg.getValue().getPrice());
    }

    @Test
    void createOfferWithBadDetailsFormat() {
        var mockOffer = mockOffer(true, true, true);
        UserDetails mockUser = mockUser(1, "test-uuid");
        when(userService.getUser())
                .thenReturn(Optional.of(mockUser));
        when(offerRepository.save(any()))
                .thenReturn(mockOffer);
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("5")
                .build());

        var mockContact = new ContactInfo("0750200100", "testLocation");
        var mockOfferInfo = new OfferInfo("BMW", "This is a used car.", ConditionType.USED, 1L,
                OfferType.FIXED_PRICE, BigDecimal.valueOf(8000), CurrencyType.EURO,
                new Timestamp(System.currentTimeMillis()), mockContact, mockListOfDetailsForACar("177.5"), 50, false, "");
        var result = offerService.createOffer(mockOfferInfo);
        assertNotNull(result);
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(BigDecimal.valueOf(20).setScale(4, RoundingMode.UP), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());

        assertCoinsChargeActionIsPerformed(mockUser);
        ArgumentCaptor<Offer> saveOfferArg = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository, times(1)).save(saveOfferArg.capture());
        assertNotNull(saveOfferArg.getValue().getExpirationDate());
        assertNotNull(saveOfferArg.getValue().getPublishDate());
        assertTrue(saveOfferArg.getValue().getPublishDate().before(saveOfferArg.getValue().getExpirationDate()));
    }

    @Test
    void createAuctionOffer() {
        var mockOffer = mockOffer(true, true, true);
        mockOffer.setOnAuction(true);
        UserDetails mockUser = mockUser(1, "test-uuid");
        when(userService.getUser())
                .thenReturn(Optional.of(mockUser));
        when(offerRepository.save(any()))
                .thenReturn(mockOffer);
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        var mockContact = new ContactInfo("0750200100", "testLocation");
        var detail = new CategoryDetails("10", "Test Detail");
        var mockOfferInfo = new OfferInfo("Test", "", ConditionType.USED, 1L,
                OfferType.AUCTION, BigDecimal.valueOf(5000), CurrencyType.RON,
                new Timestamp(LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.UTC).toEpochMilli()), mockContact, List.of(detail), 50, false, "");
        var result = offerService.createOffer(mockOfferInfo);
        assertNotNull(result);
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(mockOffer.getPrice(), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());
        assertTrue(result.isOnAuction());

        assertCoinsChargeActionIsPerformed(mockUser);
        ArgumentCaptor<Offer> saveOfferArg = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository, times(1)).save(saveOfferArg.capture());
        assertNotNull(saveOfferArg.getValue().getExpirationDate());
        assertNotNull(saveOfferArg.getValue().getAuctionEndDate());
        assertTrue(saveOfferArg.getValue().getAuctionEndDate().before(saveOfferArg.getValue().getExpirationDate()));
    }

    @Test
    void createOfferWithNullDetails() {
        var mockOffer = mockOffer(true, true, true);
        UserDetails mockUser = mockUser(1, "test-uuid");
        when(userService.getUser())
                .thenReturn(Optional.of(mockUser));
        when(offerRepository.save(any()))
                .thenReturn(mockOffer);
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());
        var mockContact = new ContactInfo("0750200100", "testLocation");
        var mockOfferInfo = new OfferInfo("Test", "", ConditionType.USED, 1L,
                OfferType.FIXED_PRICE, BigDecimal.valueOf(5000), CurrencyType.RON,
                new Timestamp(System.currentTimeMillis()), mockContact, null, 50, false, "");
        var result = offerService.createOffer(mockOfferInfo);
        assertNotNull(result);
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(mockOffer.getPrice(), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());

        assertCoinsChargeActionIsPerformed(mockUser);
    }

    @Test
    void createOfferWithNoOwnerThrowsException() {
        when(userService.getUser())
                .thenReturn(Optional.empty());
        var mockContact = new ContactInfo("0750200100", "testLocation");
        var mockOfferInfo = new OfferInfo("Test", "", ConditionType.USED, 1L,
                OfferType.FIXED_PRICE, BigDecimal.valueOf(5000), CurrencyType.RON,
                new Timestamp(System.currentTimeMillis()), mockContact, null, 10, false, "");
        assertThrows(EntityNotFoundException.class, () -> offerService.createOffer(mockOfferInfo));
    }

    @Test
    void createOfferWithInsufficientCoinsThrowsException() {
        UserDetails mockUser = mockUser(1, "test-uuid");
        mockUser.setAvailableCoins(10);
        when(userService.getUser())
                .thenReturn(Optional.of(mockUser));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        var mockContact = new ContactInfo("0750200100", "testLocation");
        var mockOfferInfo = new OfferInfo("Test", "", ConditionType.USED, 1L,
                OfferType.FIXED_PRICE, BigDecimal.valueOf(5000), CurrencyType.RON,
                new Timestamp(System.currentTimeMillis()), mockContact, null, 50, false, "");
        assertThrows(InsufficientCoinsException.class, () -> offerService.createOffer(mockOfferInfo));
    }


    @Test
    void updateProduct() {
        var mockOffer = mockOffer(false, false, false);
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        UpdateOfferInfo mockOfferInfo = mockUpdateOfferInfoDto();

        var result = offerService.updateProduct(mockOfferInfo, 1, "uuid");
        assertNotNull(result);
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(mockOffer.getPrice(), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());
    }

    @Test
    void updateProductWithContactDetails() {
        var mockOffer = mockOffer(false, false, false);
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        UpdateOfferInfo mockOfferInfo = new UpdateOfferInfo("Test", "Test Description", ConditionType.USED,
                BigDecimal.valueOf(5000), CurrencyType.RON, new ContactInfo("0751000200", "SV"), null);

        var result = offerService.updateProduct(mockOfferInfo, 1, "uuid");
        assertNotNull(result);
        assertEquals(mockOffer.getName(), result.title());
        assertEquals(mockOffer.getDescription(), result.description());
        assertEquals(mockOffer.getPrice(), result.price());
        assertEquals(mockOffer.getCategoryId(), result.categoryId());
        assertEquals(mockOffer.getCurrencyType(), result.currencyType());
        assertEquals(mockOffer.getOwner().getUsername(), result.owner().userName());
    }

    @Test
    void updateProductThrowsEntityNotFound() {
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.empty());
        var mockOfferInfo = mockUpdateOfferInfoDto();
        assertThrows(EntityNotFoundException.class, () -> offerService.updateProduct(mockOfferInfo, 1, "uuid"));
    }

    @Test
    void updateProductThrowsEditNotAllow() {
        var mockOffer = mockOffer(false, false, false);
        mockOffer.setOnAuction(true);
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));
        var mockOfferInfo = mockUpdateOfferInfoDto();
        assertThrows(ActionNotAllowException.class, () -> offerService.updateProduct(mockOfferInfo, 1, "uuid"));
    }

    @Test
    @SneakyThrows
    void uploadOfferImages() {
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer(false, false, true)));
        when(awsService.convertToFile(any()))
                .thenReturn(mock(java.io.File.class));
        when(awsService.uploadPhoto(any(), anyString(), anyInt()))
                .thenReturn("https://s3/bucket/img");
        when(sysConfiguration.getAllowedFilesNumber()).thenReturn(8);
        when(branchIOService.createDeepLink(anyInt(), anyString(), anyString()))
                .thenReturn("https://test.com");


        var files = Collections.nCopies(2, mock(MultipartFile.class)).toArray(new MultipartFile[0]);
        var result = offerService.uploadOfferImages(1, files, "uuid");

        assertTrue(result);
    }

    @Test
    void uploadOfferImagesThrowsEntityNotFound() {
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.empty());
        var files = Collections.nCopies(6, mock(MultipartFile.class)).toArray(new MultipartFile[0]);
        assertThrows(EntityNotFoundException.class, () -> offerService.uploadOfferImages(1, files, "uuid"));

    }

    @Test
    @SneakyThrows
    void uploadOfferImagesThrowsPhotoUploadException_whenConvertFileFails() {
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer(false, false, true)));
        when(awsService.convertToFile(any()))
                .thenThrow(new IOException("Test exception"));
        when(sysConfiguration.getAllowedFilesNumber()).thenReturn(8);

        var files = Collections.nCopies(2, mock(MultipartFile.class)).toArray(new MultipartFile[0]);
        assertThrows(PhotoUploadException.class, () -> offerService.uploadOfferImages(1, files, "uuid"));
    }

    @Test
    void deleteOffer() {
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer(false, false, false)));

        offerService.deleteOffer(1, "uuid");
        verify(favoriteService, times(1)).removeOfferFromFavorites(any());
        verify(offerRepository, times(1)).deleteById(anyInt());
    }

    @Test
    void deleteOfferThrowsException() {
        var mock = mockOffer(false, false, false);
        mock.setOnAuction(true);
        mock.setAuctionEndDate(new Date());
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mock));

        assertThrows(ActionNotAllowException.class, () -> offerService.deleteOffer(1, "uuid"));
    }

    @Test
    void deleteOfferThrows() {
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> offerService.deleteOffer(1, "uuid"));
        verify(offerRepository, times(0)).deleteById(anyInt());
    }

    @Test
    void changeOfferStatus() {
        var mockOffer = mockOffer(true, true, true);
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));

        var result = offerService.changeOfferState(1, 0, "uuid-test");
        assertEquals("Inactive", result.offerStatus());
    }

    @Test
    void changeStatusForAuctionOffer_shouldNotifyBidders() {
        var mockOffer = mockOffer(true, true, true);
        mockOffer.setOnAuction(true);
        mockOffer.setAuctionEndDate(new GregorianCalendar(2050, Calendar.APRIL, 11).getTime());
        Set<Bid> bids = mockBids();
        mockOffer.setBids(bids);
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));

        var result = offerService.changeOfferState(1, 0, "uuid-test");
        assertEquals("Interrupted", result.offerStatus());
        //Assert notification are send
        ArgumentCaptor<MessagePayload> notificationPayloadArgs = ArgumentCaptor.forClass(MessagePayload.class);
        verify(messageSender, times(2))
                .send(notificationPayloadArgs.capture());
        assertEquals(2, notificationPayloadArgs.getAllValues().size());
        notificationPayloadArgs.getAllValues()
                .forEach(payload -> assertEquals(MessageType.AUCTION_INTERRUPTED, payload.getMessageType()));
    }

    @Test
    void changeStatusForAuctionOffer() {
        var mockOffer = mockOffer(true, true, true);
        mockOffer.setOnAuction(true);
        mockOffer.setAuctionEndDate(new GregorianCalendar(2050, Calendar.APRIL, 11).getTime());
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));

        var result = offerService.changeOfferState(1, 0, "uuid-test");
        assertEquals("Interrupted", result.offerStatus());
    }

    @Test
    void changeStatusForInterruptedOffer_shouldFail() {
        var mockOffer = mockOffer(true, true, true);
        mockOffer.setOnAuction(true);
        mockOffer.setAvailable(false);
        mockOffer.setAuctionEndDate(new GregorianCalendar(2050, Calendar.APRIL, 11).getTime());
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));

        assertThrows(ActionNotAllowException.class, () -> offerService.changeOfferState(1, 0, "uuid-test"));
    }

    @Test
    void changeStatusForFinishedOffer_shouldFail() {
        var mockOffer = mockOffer(true, true, true);
        mockOffer.setOnAuction(true);
        mockOffer.setAvailable(false);
        mockOffer.setAuctionEndDate(new GregorianCalendar(2022, Calendar.APRIL, 11).getTime());
        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));

        assertThrows(ActionNotAllowException.class, () -> offerService.changeOfferState(1, 0, "uuid-test"));
    }

    @Test
    void shouldReactivate_FixedPriceOffer_When_RequestIsValid() {
        //Arrange
        var mockOffer = mockOffer(true, true, true);
        mockOffer.setOnAuction(false);
        mockOffer.setAvailable(false);
        Calendar calendar = Calendar.getInstance(); // Get the current date and time
        calendar.add(Calendar.DAY_OF_MONTH, -29); // Subtract 29 days from the current date
        mockOffer.setPublishDate(calendar.getTime());
        mockOffer.setAuctionEndDate(null);


        when(offerRepository.findByIdAndOwner_Username(anyInt(), anyString()))
                .thenReturn(Optional.of(mockOffer));
        //Act
        var result = offerService.changeOfferState(1, 0, "uuid-test");

        //Assert
        assertEquals(OfferStatus.ACTIVE.getStatus(), result.offerStatus());
    }

    @Test
    void should_PromoteOffer_When_RequestIsValid() {
        //Arrange
        var mockOffer = mock(Offer.class);
        var mockUser = mockUser(1, UUID.randomUUID().toString());
        when(mockOffer.isAvailable()).thenReturn(true);
        when(mockOffer.getOwner()).thenReturn(mockUser);
        when(offerRepository.findOfferById(1))
                .thenReturn(Optional.of(mockOffer));

        //Act
        offerService.promoteOffer(1, new PromoteInfo(100, new Date()));

        //Assert
        assertAll(
                () -> verify(mockOffer, times(1)).setPromoted(true),
                () -> verify(mockOffer, times(1)).setPromoteExpirationDate(any(Date.class)),
                () -> verify(userService, times(1)).updateUserCoinsNumber(anyString(), anyInt())
        );
    }

    @Test
    void should_FailPromoteOffer_When_OfferIsNotAvailable() {
        //Arrange
        var mockOffer = mock(Offer.class);
        when(mockOffer.isAvailable()).thenReturn(false);
        when(offerRepository.findOfferById(1))
                .thenReturn(Optional.of(mockOffer));

        //Act
        assertThrows(ActionNotAllowException.class, () -> offerService.promoteOffer(1, new PromoteInfo(100, new Date())));
    }

    @Test
    void should_GetListOfPromotedOffers_When_OfferPromotedPresent() {
        //Arrange
        var mockOffer = mockOffer(true, true, true);
        when(cacheUtils.getCachedOfferIds()).thenReturn(Set.of(10, 200));
        when(offerRepository.findPromotedOffersExcluding(anySet())).thenReturn(Collections.nCopies(5, mockOffer));
        when(userService.getUserFavoriteOffersIds(anyString())).thenReturn(List.of());
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        //Act
        var result = offerService.getPromotedOffers();

        //Assert
        assertAll(
                () -> verify(cacheUtils, times(1)).addOfferIds(anyList()),
                () -> assertEquals(5, result.size())
        );
    }

    @Test
    void should_MixPromotedOffers_When_OfferPromotedPresentButLessThanExpected() {
        //Arrange
        var mockOffer = mockOffer(true, true, true);
        when(cacheUtils.getCachedOfferIds()).thenReturn(Set.of(10, 200));
        when(offerRepository.findPromotedOffersExcluding(anySet())).thenReturn(new ArrayList<>(Collections.nCopies(3, mockOffer)));
        when(offerRepository.getRandomOffersExcluding(anySet(), anyInt())).thenReturn(new ArrayList<>(Collections.nCopies(2, mockOffer)));
        when(userService.getUserFavoriteOffersIds(anyString())).thenReturn(List.of());
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        //Act
        var result = offerService.getPromotedOffers();

        //Assert
        assertAll(
                () -> verify(cacheUtils, times(1)).addOfferIds(anyList()),
                () -> assertEquals(5, result.size())
        );
    }

    @Test
    void should_ReturnPromotedOffers_When_OfferPromotedPresentButIdsMarkedForExcluding() {
        //Arrange
        var mockOffer = mockOffer(true, true, true);
        when(cacheUtils.getCachedOfferIds()).thenReturn(Set.of(10, 200));
        when(offerRepository.findPromotedOffersExcluding(anySet())).thenReturn(new ArrayList<>(Collections.nCopies(0, mockOffer)));
        when(offerRepository.getRandomOffersExcluding(anySet(), anyInt())).thenReturn(new ArrayList<>(Collections.nCopies(0, mockOffer).stream().toList()));
        when(offerRepository.findAllByIdIn(anyList())).thenReturn(Collections.nCopies(2, mockOffer).stream().toList());
        when(userService.getUserFavoriteOffersIds(anyString())).thenReturn(List.of());
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        //Act
        var result = offerService.getPromotedOffers();

        //Assert
        assertAll(
                () -> verify(cacheUtils, times(1)).addOfferIds(anyList()),
                () -> assertEquals(2, result.size())
        );
    }

    @Test
    void should_ReturnAvailableOnAuctionOffer_When_MethodIsCalled() {
        //Arrange
        when(offerRepository.findByIdAndAvailableTrueAndOnAuctionTrue(anyInt()))
                .thenReturn(Optional.of(mockOffer(true, true, true)));

        //Act
        var result = offerService.findAvailableOnAuctionOfferById(1);

        //Assert
        assertNotNull(result);
    }

    @Test
    void should_ThrowEntityNotFound_When_OfferNotPresent() {
        //Arrange
        when(offerRepository.findByIdAndAvailableTrueAndOnAuctionTrue(anyInt()))
                .thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(EntityNotFoundException.class, () -> offerService.findAvailableOnAuctionOfferById(1));
    }

    @Test
    void should_ThrowException_WhenPriceFilterIsSetWrong() {
        //Arrange
        var searchCriteria = new OfferSearch(OfferTestMock.OFFER_CATEGORIES, null, OfferTestMock.OFFER_TITLE, null,
                new PriceFilter(BigDecimal.valueOf(200L), BigDecimal.valueOf(100L), null), null, null);
        //Act
        assertThrows(BadRequestException.class, () -> offerService.advancedSearch(searchCriteria));
    }

    @Test
    @SneakyThrows
    void should_ReturnListOfEntities_WhenFiltersAreSet() {
        //Arrange
        var searchCriteria = new OfferSearch(OfferTestMock.OFFER_CATEGORIES, null, OfferTestMock.OFFER_TITLE, null,
                new PriceFilter(BigDecimal.valueOf(200L), BigDecimal.valueOf(100L), CurrencyType.EURO), null, null);
        when(offerRepository.findAll(any(AdvancedOfferSpecification.class)))
                .thenReturn(Collections.nCopies(5, mockOffer(true, true, true)));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        //Act
        var result = offerService.advancedSearch(searchCriteria);

        //Assert
        ArgumentCaptor<String> currencyArg = ArgumentCaptor.forClass(String.class);
        assertAll(
                () -> assertEquals(5, result.size()),
                () -> verify(sysConfiguration, times(6)).getSystemCurrency(currencyArg.capture()),
                () -> assertEquals(CurrencyType.EURO.name(), currencyArg.getAllValues().get(0))
        );
    }

    @Test
    @SneakyThrows
    void should_ReturnListOfEntities_WhenFiltersAreSetWithoutPrice() {
        //Arrange
        var searchCriteria = new OfferSearch(OfferTestMock.OFFER_CATEGORIES, null, OfferTestMock.OFFER_TITLE, null,
                null, null, null);
        when(offerRepository.findAll(any(AdvancedOfferSpecification.class)))
                .thenReturn(Collections.nCopies(5, mockOffer(true, true, true)));
        when(sysConfiguration.getSystemCurrency(anyString())).thenReturn(SystemConfiguration.builder()
                .value("1")
                .build());

        //Act
        var result = offerService.advancedSearch(searchCriteria);

        //Assert
        ArgumentCaptor<String> currencyArg = ArgumentCaptor.forClass(String.class);
        assertAll(
                () -> assertEquals(5, result.size()),
                () -> verify(sysConfiguration, times(5)).getSystemCurrency(currencyArg.capture()),
                () -> assertEquals(CurrencyType.RON.name(), currencyArg.getAllValues().get(0))
        );
    }


    private void assertCoinsChargeActionIsPerformed(UserDetails mockUser) {
        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> remainingCoinsArg = ArgumentCaptor.forClass(Integer.class);
        verify(userService, times(1)).updateUserCoinsNumber(uuidArg.capture(), remainingCoinsArg.capture());
        assertEquals(mockUser.getUsername(), uuidArg.getValue());
        assertEquals(mockUser.getAvailableCoins() - 50, remainingCoinsArg.getValue());
    }

    private Offer mockOffer(boolean addAddress,
                            boolean addContact,
                            boolean addFile) {
        return mockOffer(CurrencyType.RON.name(), addAddress, addContact, addFile);
    }

    private Offer mockOffer(
            String currency,
            boolean addAddress,
            boolean addContact,
            boolean addFile
    ) {
        Offer offer = new Offer();
        offer.setId(1);
        offer.setPrice(BigDecimal.valueOf(100).setScale(4, RoundingMode.UP));
        offer.setName("Test Item");
        offer.setDescription("This is a test.");
        offer.setCondition(ConditionType.NEW);
        offer.setCategoryId(1L);
        offer.setOnAuction(false);
        offer.setOwner(mockUser(1, "test-uuid"));
        offer.setAvailable(true);
        offer.setCurrencyType(currency);
        offer.setPublishDate(new Date());
        if (addAddress) {
            Address address = mockAddress();
            offer.addAddress(address);
        }
        if (addContact) {
            Contact contact = mockContact();
            offer.addContact(contact);
        }
        if (addFile) {
            File file = mockFile();
            offer.addFile(file);
        }
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

    private Address mockAddress() {
        Address address = new Address();
        address.setId(1);
        address.setCountry("RO");
        address.setCity("TestCity");
        address.setStreet("TestStr");

        return address;
    }

    private Contact mockContact() {
        Contact contact = new Contact();
        contact.setId(1);
        contact.setType(ContactType.PHONE);
        contact.setValue("0710500400");

        return contact;
    }

    private File mockFile() {
        File file = new File();
        file.setId(1);
        file.setUrl("link");
        file.setPrimary(true);

        return file;
    }

    private UpdateOfferInfo mockUpdateOfferInfoDto() {
        return new UpdateOfferInfo("Test", "Test Description", ConditionType.USED,
                BigDecimal.valueOf(5000), CurrencyType.RON, null, Collections.emptyList());
    }

    private List<CategoryDetails> mockListOfDetailsForACar(String powerValue) {
        List<CategoryDetails> detailsInfos = new ArrayList<>();
        CategoryDetails make = new CategoryDetails("make", "BMW");
        CategoryDetails color = new CategoryDetails("color", "Red");
        CategoryDetails model = new CategoryDetails("model", "X1");
        CategoryDetails power = new CategoryDetails("power", powerValue);
        detailsInfos.add(make);
        detailsInfos.add(color);
        detailsInfos.add(model);
        detailsInfos.add(power);

        return detailsInfos;
    }

    private Set<Bid> mockBids() {
        Set<Bid> bids = new HashSet<>();
        var firstBid = new Bid();
        firstBid.setId(1);
        firstBid.setBidValue(BigDecimal.valueOf(200));
        firstBid.setOwner(mockUser(1, "test-uuid"));
        bids.add(firstBid);

        var secondBid = new Bid();
        secondBid.setId(2);
        secondBid.setBidValue(BigDecimal.valueOf(240));
        secondBid.setOwner(mockUser(1, "test-uuid"));
        bids.add(secondBid);


        var thirdBid = new Bid();
        thirdBid.setId(3);
        thirdBid.setBidValue(BigDecimal.valueOf(220));
        thirdBid.setOwner(mockUser(2, "test-uuid-2"));
        bids.add(thirdBid);

        return bids;
    }
}
