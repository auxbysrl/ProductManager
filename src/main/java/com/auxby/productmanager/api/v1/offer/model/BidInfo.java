package com.auxby.productmanager.api.v1.offer.model;

import java.math.BigDecimal;
import java.util.Date;

public record BidInfo(
        String userName,
        String email,
        BigDecimal bidValue,
        String userAvatar,
        Date bidDate,
        boolean winner,
        String phone,
        Date lastSeen) {
}
