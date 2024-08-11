package com.auxby.productmanager.api.v1.offer;

import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.category.CategoryService;
import com.auxby.productmanager.api.v1.category.dto.CategoryInfoDto;
import com.auxby.productmanager.api.v1.commun.entity.Address;
import com.auxby.productmanager.api.v1.commun.entity.Contact;
import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import com.auxby.productmanager.api.v1.commun.system_configuration.SystemConfiguration;
import com.auxby.productmanager.api.v1.commun.system_configuration.SystemConfigurationService;
import com.auxby.productmanager.api.v1.favorite.FavoriteService;
import com.auxby.productmanager.api.v1.notification.NotificationService;
import com.auxby.productmanager.api.v1.offer.model.*;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.offer.repository.OfferData;
import com.auxby.productmanager.api.v1.offer.repository.OfferRepository;
import com.auxby.productmanager.api.v1.offer.specification.AdvancedOfferSpecification;
import com.auxby.productmanager.api.v1.offer.specification.OfferSpecification;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearch;
import com.auxby.productmanager.api.v1.offer.specification.criteria.OfferSearchCriteria;
import com.auxby.productmanager.api.v1.user.UserService;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.config.cache.CacheUtils;
import com.auxby.productmanager.exception.ActionNotAllowException;
import com.auxby.productmanager.exception.DeepLinkGenerationException;
import com.auxby.productmanager.exception.InsufficientCoinsException;
import com.auxby.productmanager.exception.PhotoUploadException;
import com.auxby.productmanager.rabbitmq.MessageSender;
import com.auxby.productmanager.rabbitmq.message.MessagePayload;
import com.auxby.productmanager.rabbitmq.message.MessageType;
import com.auxby.productmanager.utils.DateTimeProcessor;
import com.auxby.productmanager.utils.SecurityContextUtil;
import com.auxby.productmanager.utils.SimplePage;
import com.auxby.productmanager.utils.enums.ContactType;
import com.auxby.productmanager.utils.enums.CurrencyType;
import com.auxby.productmanager.utils.enums.OfferStatus;
import com.auxby.productmanager.utils.enums.OfferType;
import com.auxby.productmanager.utils.mapper.OfferMapper;
import com.auxby.productmanager.utils.service.AmazonClientService;
import com.auxby.productmanager.utils.service.BranchIOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.auxby.productmanager.rabbitmq.message.MessageParams.OFFER_NAME;
import static com.auxby.productmanager.utils.DateTimeProcessor.*;
import static com.auxby.productmanager.utils.constant.AppConstant.OFFER_VALID_NUMBER_OF_DAYS;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfferService {
    private static final String DEFAULT = "default";
    private final OfferMapper offerMapper;
    private final OfferRepository offerRepository;
    private final UserService userService;
    private final AmazonClientService awsService;
    private final FavoriteService favoriteService;
    private final MessageSender messageSender;
    private final NotificationService notificationService;
    private final CategoryService categoryService;
    private final CacheUtils cacheUtils;
    private final SystemConfigurationService sysConfiguration;
    private final BranchIOService branchIOService;

    public SimplePage<OfferSummary> getAllOffers(Pageable pageable, OfferSearchCriteria searchCriteria) {
        Page<Offer> page = offerRepository.findAll(new OfferSpecification(searchCriteria), pageable);
        List<Integer> userFavoriteOffers = getFavoriteOffersIds(page.getContent());
        List<OfferSummary> products = page.getContent()
                .stream()
                .map(offer -> mapOfferToSummary(offer, userFavoriteOffers))
                .toList();

        return new SimplePage<>(products, page.getTotalElements(), pageable);
    }

    public List<OfferSummary> getOffersByIds(List<Integer> ids) {
        var offers = offerRepository.findAllByIdIn(ids);
        List<Integer> userFavoriteOffers = getFavoriteOffersIds(offers);

        return offers.stream()
                .map(offer -> mapOfferToSummary(offer, userFavoriteOffers))
                .toList();
    }

    public List<OfferSummary> getAllUserOffers(String userUuid) {
        var offers = offerRepository.findByOwner_Username(userUuid);
        List<Integer> userFavoriteOffers = getFavoriteOffersIds(offers);

        return offers.stream()
                .map(offer -> mapOfferToSummary(offer, userFavoriteOffers))
                .toList();
    }

    @Transactional
    public DetailedOfferResponse getOfferById(Integer id, boolean increaseView) {
        Offer offer = offerRepository.findOfferById(id)
                .orElseThrow(() -> new EntityNotFoundException("Offer with key:" + id + " not found."));

        try {
            notificationService.deleteOfferNotifications(offer.getId());
        } catch (Exception ex) {
            log.info("Failed to delete notifications for offer:" + offer.getName());
        }

        if (increaseView) {
            offer.increaseViewNumber();
        }

        return mapOfferToDetailedOffer(offer);
    }

    @Transactional
    public OfferSummary createOffer(OfferInfo offerInfo) {
        UserDetails owner = userService.getUser()
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        Offer newOffer = getOfferWithData(offerInfo);
        chargeCoins(owner, offerInfo.requiredCoins());
        newOffer.setOwner(owner);
        newOffer.addAddress(getAddress(offerInfo.contactInfo().location()));
        newOffer.addContact(getContactPhone(offerInfo.contactInfo().phoneNumber()));

        if (offerInfo.categoryDetails() != null) {
            List<OfferData> data = offerInfo.categoryDetails()
                    .stream()
                    .map(this::mapToOfferDetail)
                    .toList();
            data.forEach(newOffer::addData);
        }
        Offer savedOffer = offerRepository.save(newOffer);

        return mapOfferToSummary(savedOffer, Collections.emptyList());
    }

    @Transactional
    public OfferSummary updateProduct(UpdateOfferInfo offerInfo, Integer id, String userUuid) {
        Offer offer = getOwnerOfferById(id, userUuid);
        if (offer.isOnAuction()) {
            throw new ActionNotAllowException();
        }
        offer.setName(offerInfo.title());
        offer.computeDBPrice(offerInfo.price(), sysConfiguration.getSystemCurrency(offerInfo.currencyType().name()));
        offer.setDescription(offerInfo.description());
        offer.setCondition(offerInfo.conditionType());
        offer.setCurrencyType(offerInfo.currencyType().name());
        offer.getContacts().forEach(offer::removeContact);
        offer.getAddresses().forEach(offer::removeAddress);
        Set<OfferData> oldOfferDetails = new HashSet<>(offer.getOfferDetails());
        oldOfferDetails.forEach(offer::removeData);

        if (offerInfo.categoryDetails() != null) {
            List<OfferData> data = offerInfo.categoryDetails()
                    .stream()
                    .map(this::mapToOfferDetail)
                    .toList();
            data.forEach(offer::addData);
        }
        if (Objects.nonNull(offerInfo.contactInfo())) {
            offer.addAddress(getAddress(offerInfo.contactInfo().location()));
            offer.addContact(getContactPhone(offerInfo.contactInfo().phoneNumber()));
        }

        return mapOfferToSummary(offer, Collections.emptyList());
    }

    public List<CategoryInfo> getOfferCategory() {
        List<CategoryInfoDto> categories = categoryService.getCategories();
        if (Objects.isNull(categories)) {
            return new ArrayList<>();
        }

        return categories.stream()
                .map(c -> {
                    long count = offerRepository.countActiveOffersByCategoryId(c.id());
                    var localization = c.label().stream().map(l -> new Localization(l.language(), l.translation())).toList();
                    return CategoryInfo.builder()
                            .noOffers(count)
                            .icon(c.icon())
                            .label(localization)
                            .id(c.id())
                            .build();
                })
                .toList();
    }

    @Transactional
    public boolean uploadOfferImages(Integer offerId,
                                     MultipartFile[] files,
                                     String userUuid) {
        Offer offer = getOwnerOfferById(offerId, userUuid);
        List<com.auxby.productmanager.api.v1.commun.entity.File> existingOfferImages = new ArrayList<>(offer.getFiles());
        if (files.length > sysConfiguration.getAllowedFilesNumber()) {
            throw new PhotoUploadException("Offer images is limit to 6 photos.");
        }
        awsService.deleteOfferResources(userUuid, offerId);
        List<com.auxby.productmanager.api.v1.commun.entity.File> offerImages = uploadImages(offerId, files, userUuid);
        var primary = offerImages.stream()
                .filter(com.auxby.productmanager.api.v1.commun.entity.File::isPrimary)
                .findFirst();
        if (primary.isEmpty()) {
            offerImages.get(0).setPrimary(true);
        }
        offerImages.forEach(offer::addFile);
        existingOfferImages.forEach(offer::removeFile);
        try {
            var deepLink = generateDeepLink(offerId, offer);
            offer.setDeepLink(deepLink);
        } catch (DeepLinkGenerationException e) {
            log.info("Fail to generate deep link for offer:{}", offer.getName());
        }

        return true;
    }

    @Transactional
    public OfferStatusInfo changeOfferState(Integer offerId,
                                            Integer requiredCoins,
                                            String userUuid) {
        Offer offer = getOwnerOfferById(offerId, userUuid);
        validateChangeStatusAction(offer);
        if (!offer.isOnAuction()) {
            long daysSincePublished = computeNumberOfDaysSincePublished(offer.getPublishDate());
            if (daysSincePublished > OFFER_VALID_NUMBER_OF_DAYS && !offer.isAvailable()) {
                chargeCoins(offer.getOwner(), requiredCoins);
            }
            Date publishDate = new Date();
            offer.setPublishDate(publishDate);
            offer.setExpirationDate(computeOfferExpirationDate(publishDate));
        } else if (offer.isAvailable() && !offer.getBids().isEmpty()) {
            var userBidsMap = offer.getBids()
                    .stream()
                    .collect(Collectors.groupingBy(b -> b.getOwner().getUsername()));
            userBidsMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .forEach(offer::removeBid);
            userBidsMap.keySet()
                    .forEach(uuid -> notifyBidder(offerId, offer.getName(), uuid));
        }
        if (offer.isOnAuction()) {
            favoriteService.removeOfferFromFavorites(offerId);
        }
        offer.setAvailable(!offer.isAvailable());
        return new OfferStatusInfo(offerId, getOfferStatus(offer));
    }

    @Transactional
    public boolean deleteOffer(Integer id, String userUuid) {
        Offer offer = getOwnerOfferById(id, userUuid);
        boolean isOnActiveAuction = offer.isOnAuction() && offer.isAvailable();
        if (offer.isAvailable() && isOnActiveAuction) {
            throw new ActionNotAllowException("Deleting an active auction offer is not allow.");
        }
        delete(id, userUuid);
        return true;
    }

    public Offer findAvailableOnAuctionOfferById(Integer id) {
        return offerRepository.findByIdAndAvailableTrueAndOnAuctionTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Valid on auction offer not found with key:" + id));
    }

    public List<OfferSummary> advancedSearch(OfferSearch searchCriteria) throws BadRequestException {
        validateSearchCriteria(searchCriteria);
        SystemConfiguration configuredCurrency = (searchCriteria.priceFilter() != null)
                ? sysConfiguration.getSystemCurrency(searchCriteria.priceFilter().currencyType().name())
                : null;

        AdvancedOfferSpecification specification = new AdvancedOfferSpecification(searchCriteria, configuredCurrency);

        var offers = offerRepository.findAll(specification);
        List<Integer> userFavoriteOffers = getFavoriteOffersIds(offers);

        return offers.stream()
                .map(offer -> mapOfferToSummary(offer, userFavoriteOffers))
                .toList();
    }

    public void addRemoveFavoriteOffer(Integer offerId) {
        UserDetails userDetails = userService.getUser()
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        favoriteService.addRemoveOfferFromFavorite(userDetails.getId(), offerId);
    }

    public List<OfferSummary> getFavoriteOffers() {
        var favoriteOffersIds = userService.getUserFavoriteOffersIds(SecurityContextUtil.getUsername());
        var offers = offerRepository.findAllByIdIn(favoriteOffersIds);
        List<Integer> userFavoriteOffers = new ArrayList<>();
        if (!offers.isEmpty()) {
            userFavoriteOffers.addAll(favoriteOffersIds);
        }

        return offers.stream()
                .map(offer -> mapOfferToSummary(offer, userFavoriteOffers))
                .toList();
    }

    @Transactional
    public void promoteOffer(Integer offerId,
                             PromoteInfo promoteInfo) {
        Offer offer = offerRepository.findOfferById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found."));
        if (!offer.isAvailable()) {
            throw new ActionNotAllowException("Promote an offer that is no longer available is not allowed.");
        }
        chargeCoins(offer.getOwner(), promoteInfo.requiredCoins());
        offer.setPromoted(true);
        offer.setPromoteExpirationDate(promoteInfo.expirationDate());
    }

    public SearchSummaryInfo searchOfferInCategories(String offerTitle) {
        var offersMap = offerRepository.findAllOffersByTitleLike(offerTitle.toLowerCase())
                .stream()
                .collect(Collectors.groupingBy(Offer::getCategoryId));
        Map<Long, Integer> result = new HashMap<>();
        offersMap.forEach((k, v) -> result.put(k, v.size()));

        return new SearchSummaryInfo(result);
    }

    @Cacheable(cacheNames = "shortLivedCache")
    public List<OfferSummary> getPromotedOffers() {
        Set<Integer> cachedOfferIds = cacheUtils.getCachedOfferIds();
        var promotedOffers = offerRepository.findPromotedOffersExcluding(cachedOfferIds);
        if (promotedOffers.size() < 5) {
            promotedOffers.addAll(getExtraOffers(cachedOfferIds, promotedOffers.size()));
        }
        cacheOfferIds(promotedOffers);
        List<Integer> userFavoriteOffers = getFavoriteOffersIds(promotedOffers);

        return promotedOffers.stream()
                .map(offer -> mapOfferToSummary(offer, userFavoriteOffers))
                .toList();
    }

    public OfferSummary mapOfferToSummary(Offer offer,
                                          List<Integer> userFavoriteOffers) {
        Optional<Address> address = offer.getAddresses()
                .stream()
                .findFirst();
        String location = address.isPresent() ? address.get().getCity() : "";
        Set<BidInfo> offerBids = getOfferBidsList(offer);
        BigDecimal highestBid = computeHighestBid(offer);
        Boolean isUserFavorite = userFavoriteOffers != null ? userFavoriteOffers.contains(offer.getId()) : null;

        return offerMapper.mapToOfferSummary(offer, location, offerBids, highestBid, isUserFavorite, getOfferStatus(offer), offer.isPromoted(), computePrice(offer));
    }

    public BigDecimal computePrice(Offer offer) {
        CurrencyType displayCurrency = CurrencyType.valueOf(offer.getCurrencyType());
        return displayCurrency.getCurrencyPrice(offer.getPrice(), sysConfiguration.getSystemCurrency(displayCurrency.name()));
    }

    @Transactional
    public String generateDeepLink(Integer offerId) {
        var offer = offerRepository.findOfferById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found."));
        if (StringUtils.hasText(offer.getDeepLink())) {
            return offer.getDeepLink();
        }
        var deepLink = generateDeepLink(offerId, offer);
        offer.setDeepLink(deepLink);

        return deepLink;
    }

    private List<Offer> getExtraOffers(Set<Integer> cachedOfferIds, int size) {
        var limit = 5 - size;
        var offers = offerRepository.getRandomOffersExcluding(cachedOfferIds, limit);
        List<Offer> promotedOffers = new ArrayList<>(offers);
        if (!cachedOfferIds.isEmpty() && promotedOffers.isEmpty()) {
            var oldOffers = offerRepository.findAllByIdIn(cachedOfferIds.stream().toList());
            promotedOffers.addAll(oldOffers);
        }

        return promotedOffers;
    }

    private void cacheOfferIds(List<Offer> promotedOffers) {
        var ids = promotedOffers.stream()
                .map(AuxbyBaseEntity::getId)
                .toList();
        cacheUtils.addOfferIds(ids);
    }

    private List<Integer> getFavoriteOffersIds(List<Offer> offers) {
        List<Integer> userFavoriteOffers = new ArrayList<>();
        if (!offers.isEmpty()) {
            userFavoriteOffers.addAll(userService.getUserFavoriteOffersIds(SecurityContextUtil.getUsername()));
        }

        return userFavoriteOffers;
    }

    private void notifyBidder(Integer offerId,
                              String offerName,
                              String bidderUuid) {
        var message = MessagePayload.builder()
                .receiver(bidderUuid)
                .offerId(offerId)
                .messageType(MessageType.AUCTION_INTERRUPTED)
                .messageExtraInfo(Map.of(OFFER_NAME.name(), offerName))
                .build();
        messageSender.send(message);
    }

    private void delete(Integer id, String userUuid) {
        favoriteService.removeOfferFromFavorites(id);
        offerRepository.deleteById(id);
        awsService.deleteOfferResources(userUuid, id);
    }

    private void validateChangeStatusAction(Offer offer) {
        if (offer.isOnAuction()) {
            boolean isAuctionFinished = isDateInThePast(offer.getAuctionEndDate());
            if (isAuctionFinished) {
                throw new ActionNotAllowException("Edit not allow. Offer from finished auctions cannot be updated.");
            }
            if (!offer.isAvailable()) {
                throw new ActionNotAllowException("Edit not allow. Interrupted auctions cannot be activated.");
            }
        }
    }

    private Offer getOwnerOfferById(Integer offerId, String userUuid) {
        return offerRepository.findByIdAndOwner_Username(offerId, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Offer with key:" + offerId + " not found for this user."));
    }

    private String processImage(Integer offerId,
                                String ownerUuid,
                                MultipartFile photo) {
        try {
            File file = awsService.convertToFile(photo);
            return awsService.uploadPhoto(file, ownerUuid, offerId);
        } catch (IOException e) {
            throw new PhotoUploadException();
        }
    }

    private Offer getOfferWithData(OfferInfo offerInfo) {
        Offer newOffer = offerMapper.mapToOffer(offerInfo);
        newOffer.setAvailable(true);
        newOffer.computeDBPrice(offerInfo.price(), sysConfiguration.getSystemCurrency(offerInfo.currencyType().name()));
        Date publishDate = new Date();
        newOffer.setPublishDate(publishDate);

        if (offerInfo.offerType() == OfferType.FIXED_PRICE) {
            newOffer.setOnAuction(false);
            newOffer.setAutoExtend(offerInfo.autoExtend());
            newOffer.setCoinsToExtend(offerInfo.requiredCoins());
            newOffer.setExpirationDate(computeOfferExpirationDate(publishDate));
            newOffer.setAuctionEndDate(null);
            newOffer.setAuctionStartDate(null);
        } else {
            if (Objects.isNull(offerInfo.auctionEndDate()) || isDateInThePast(offerInfo.auctionEndDate())) {
                throw new ActionNotAllowException("Auction end date cannot be in the past.");
            }
            newOffer.setOnAuction(true);
            newOffer.setAuctionStartDate(publishDate);
            newOffer.setExpirationDate(computeOfferExpirationDate(offerInfo.auctionEndDate()));
            newOffer.setAuctionEndDate(computeAuctionExpirationDate(offerInfo.auctionEndDate()));
        }
        newOffer.setCategoryId(offerInfo.categoryId());
        newOffer.setCondition(offerInfo.conditionType());
        newOffer.setViewsNumber(0);
        newOffer.setPromoted(false);

        return newOffer;
    }

    private OfferData mapToOfferDetail(CategoryDetails detail) {
        String value;
        if (detail.value().matches("-?\\d+(\\.\\d+)?")) {
            value = String.valueOf((int) Math.floor(Double.parseDouble(detail.value())));
        } else {
            value = detail.value();
        }
        OfferData offerData = new OfferData();
        offerData.setFieldId(detail.key());
        offerData.setValue(value);
        offerData.setKey(detail.key());

        return offerData;
    }

    private Contact getContactPhone(String phoneNumber) {
        Contact contact = new Contact();
        contact.setValue(phoneNumber);
        contact.setType(ContactType.PHONE);

        return contact;
    }

    private Address getAddress(String location) {
        Address address = new Address();
        address.setCity(location);
        address.setCountry("");
        address.setStreet("");

        return address;
    }

    private BigDecimal computeHighestBid(Offer offer) {
        if (!offer.isOnAuction() || offer.getBids().isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            var sums = offer.getBids()
                    .stream()
                    .map(Bid::getBidValue)
                    .toList();
            return Collections.max(sums);
        }
    }

    private Set<BidInfo> getOfferBidsList(Offer offer) {
        if (!offer.isOnAuction()) {
            return new HashSet<>();
        }
        return offer.getBids()
                .stream()
                .map(bid -> new BidInfo(bid.getBidderName(), bid.getOwner().getUsername(), bid.getBidValue(), bid.getOwner().getAvatarUrl(),
                        bid.getDate(), Objects.nonNull(bid.getIsWinner()) ? bid.getIsWinner() : false, getPhoneNumber(bid.getOwner().getContacts()), bid.getOwner().getLastSeen()))
                .collect(Collectors.toSet());
    }

    private DetailedOfferResponse mapOfferToDetailedOffer(Offer offer) {
        Boolean isFavorite = userService.getUserFavoriteOffersIds(SecurityContextUtil.getUsername())
                .contains(offer.getId());
        Optional<Address> address = offer.getAddresses()
                .stream()
                .findFirst();
        String location = address.isPresent() ? address.get().getCity() : "";

        return offerMapper.mapToDetailedOffer(offer, location, getOfferBidsList(offer), computeHighestBid(offer), isFavorite, getOfferStatus(offer), offer.isPromoted(), offer.getPhoneNumbersAsString(), computePrice(offer));
    }

    private String getOfferStatus(Offer offer) {
        if (offer.isOnAuction()) {
            if (offer.isAvailable()) {
                return OfferStatus.ACTIVE.getStatus();
            }
            if (DateTimeProcessor.isDateInThePast(offer.getAuctionEndDate())) {
                return OfferStatus.FINISHED.getStatus();
            } else {
                return OfferStatus.INTERRUPTED.getStatus();
            }
        } else {
            return offer.isAvailable() ? OfferStatus.ACTIVE.getStatus() : OfferStatus.INACTIVE.getStatus();
        }
    }

    private void chargeCoins(UserDetails userDetails, Integer coinsNeeded) {
        if (userDetails.getAvailableCoins() < coinsNeeded) {
            throw new InsufficientCoinsException("Add new offer");
        }
        Integer remainingCoins = userDetails.getAvailableCoins() - coinsNeeded;
        userService.updateUserCoinsNumber(userDetails.getUsername(), remainingCoins);
    }

    private com.auxby.productmanager.api.v1.commun.entity.File getOfferImage(List<com.auxby.productmanager.api.v1.commun.entity.File> offerImages,
                                                                             MultipartFile photo,
                                                                             String url) {
        com.auxby.productmanager.api.v1.commun.entity.File file = new com.auxby.productmanager.api.v1.commun.entity.File();
        var primary = offerImages.stream()
                .filter(com.auxby.productmanager.api.v1.commun.entity.File::isPrimary)
                .findFirst();
        if (Objects.nonNull(photo.getOriginalFilename()) &&
                Objects.requireNonNull(photo.getOriginalFilename()).contains(DEFAULT) &&
                primary.isEmpty()) {
            file.setPrimary(true);
        }
        file.setUrl(url);
        return file;
    }

    private String getPhoneNumber(Set<Contact> contacts) {
        Contact phone = contacts.stream()
                .filter(c -> c.getType() == ContactType.PHONE)
                .findFirst()
                .orElse(null);
        if (phone == null) {
            return "";
        }
        return phone.getValue();
    }

    private void validateSearchCriteria(OfferSearch searchCriteria) throws BadRequestException {
        if (Objects.nonNull(searchCriteria.priceFilter()) && Objects.isNull(searchCriteria.priceFilter().currencyType())) {
            throw new BadRequestException("Currency type is mandatory for price filter.");
        }
    }

    private List<com.auxby.productmanager.api.v1.commun.entity.File> uploadImages(Integer offerId, MultipartFile[] files, String userUuid) {
        List<com.auxby.productmanager.api.v1.commun.entity.File> offerImages = new ArrayList<>();
        Arrays.stream(files)
                .forEach(photo -> {
                    String url = processImage(offerId, userUuid, photo);
                    com.auxby.productmanager.api.v1.commun.entity.File file = getOfferImage(offerImages, photo, url);
                    offerImages.add(file);
                });

        return offerImages;
    }

    private String generateDeepLink(Integer offerId, Offer offer) {
        var primaryFile = offer.getMainImage();
        var url = primaryFile.map(com.auxby.productmanager.api.v1.commun.entity.File::getUrl).orElse("");
        return branchIOService.createDeepLink(offerId, offer.getName(), url);
    }
}
