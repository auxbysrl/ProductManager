package com.auxby.productmanager.api.v1.offer.model;

import com.auxby.productmanager.api.v1.user.model.UserDetails;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Builder
public record OfferSummary(
        Long id,
        String title,
        String location,
        String description,
        Long categoryId,
        Date publishDate,
        Date expirationDate,
        Date auctionStartDate,
        Date auctionEndDate,
        boolean isOnAuction,
        BigDecimal highestBid,
        List<BidInfo> bids,
        BigDecimal price,
        String currencyType,
        UserDetails owner,
        List<FileInfo> photos,
        Boolean isUserFavorite,
        String condition,
        String status,
        boolean isPromoted,
        boolean autoExtend
) {
}
