package com.auxby.productmanager.utils.mapper;

import com.auxby.productmanager.api.v1.offer.model.BidInfo;
import com.auxby.productmanager.api.v1.offer.model.DetailedOfferResponse;
import com.auxby.productmanager.api.v1.offer.model.OfferInfo;
import com.auxby.productmanager.api.v1.offer.model.OfferSummary;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.user.model.UserDetails;
import com.auxby.productmanager.utils.enums.CurrencyType;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Set;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public interface OfferMapper {

    @Mapping(target = "bids", source = "bids")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "title", source = "offer.name")
    @Mapping(target = "highestBid", source = "highestBid", defaultValue = "0.0")
    @Mapping(target = "isOnAuction", source = "offer.onAuction", defaultValue = "false")
    @Mapping(target = "photos", source = "offer.files")
    @Mapping(target = "isUserFavorite", source = "isFavorite")
    @Mapping(target = "isPromoted", source = "isPromoted")
    @Mapping(target = "price", source = "displayPrice")
    @Mapping(target = "owner", expression = "java(toUserDetails(offer))")
    @Mapping(target = "currencySymbol", expression = "java(getSymbol(offer))")
    OfferSummary mapToOfferSummary(Offer offer,
                                   String location,
                                   Set<BidInfo> bids,
                                   BigDecimal highestBid,
                                   Boolean isFavorite,
                                   String status,
                                   Boolean isPromoted,
                                   BigDecimal displayPrice);

    @Mapping(target = "bids", source = "bids")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "title", source = "offer.name")
    @Mapping(target = "highestBid", source = "highestBid", defaultValue = "0.0")
    @Mapping(target = "isOnAuction", source = "offer.onAuction", defaultValue = "false")
    @Mapping(target = "photos", source = "offer.files")
    @Mapping(target = "details", source = "offer.offerDetails")
    @Mapping(target = "isUserFavorite", source = "isUserFavorite")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "isPromoted", source = "isPromoted")
    @Mapping(target = "price", source = "displayPrice")
    @Mapping(target = "owner", expression = "java(toUserDetails(offer))")
    @Mapping(target = "currencySymbol", expression = "java(getSymbol(offer))")
    DetailedOfferResponse mapToDetailedOffer(Offer offer,
                                             String location,
                                             Set<BidInfo> bids,
                                             BigDecimal highestBid,
                                             Boolean isUserFavorite,
                                             String status,
                                             Boolean isPromoted,
                                             String phoneNumbers,
                                             BigDecimal displayPrice);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "price", ignore = true)
    Offer mapToOffer(OfferInfo offerInfo);

    default String getSymbol(Offer offer) {
        try {
            CurrencyType type = CurrencyType.getCurrencyType(offer.getCurrencyType());
            return type.getSymbol();
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return "lei";
        }
    }

    default UserDetails toUserDetails(Offer offer) {
        return UserDetails.builder()
                .avatarUrl(offer.getOwner().getAvatarUrl())
                .firstName(offer.getOwner().getFirstName())
                .lastName(offer.getOwner().getLastName())
                .userName(offer.getOwner().getUsername())
                .rating(offer.getOwner().getUserRating())
                .lastSeen(offer.getOwner().getLastSeen())
                .build();
    }
}
