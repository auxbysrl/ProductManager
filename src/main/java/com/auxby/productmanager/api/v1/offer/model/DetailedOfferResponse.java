package com.auxby.productmanager.api.v1.offer.model;

import com.auxby.productmanager.api.v1.user.model.UserDetails;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record DetailedOfferResponse(Long id,
                                    String title,
                                    String location,
                                    String description,
                                    Long categoryId,
                                    Date publishDate,
                                    Date expirationDate,
                                    Date auctionStartDate,
                                    Date auctionEndDate,
                                    boolean isOnAuction,
                                    BigDecimal price,
                                    String currencyType,
                                    String currencySymbol,
                                    UserDetails owner,
                                    BigDecimal highestBid,
                                    List<BidInfo> bids,
                                    List<FileInfo> photos,
                                    List<OfferDetailsInfo> details,
                                    Integer viewsNumber,
                                    Integer setAsFavoriteNumber,
                                    Boolean isUserFavorite,
                                    String status,
                                    boolean isPromoted,
                                    boolean autoExtend,
                                    String condition,
                                    String phoneNumbers,
                                    String deepLink
) {
}
