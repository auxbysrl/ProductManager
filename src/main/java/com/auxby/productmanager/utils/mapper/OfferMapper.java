package com.auxby.productmanager.utils.mapper;

import com.auxby.productmanager.api.v1.offer.model.BidInfo;
import com.auxby.productmanager.api.v1.offer.model.DetailedOfferResponse;
import com.auxby.productmanager.api.v1.offer.model.OfferInfo;
import com.auxby.productmanager.api.v1.offer.model.OfferSummary;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    @Mapping(target = "owner.userName", source = "offer.owner.username")
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
    @Mapping(target = "owner.userName", source = "offer.owner.username")
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
}
