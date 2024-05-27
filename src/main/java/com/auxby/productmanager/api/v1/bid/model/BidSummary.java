package com.auxby.productmanager.api.v1.bid.model;

import java.math.BigDecimal;

public record BidSummary(String user,
                         BigDecimal amount
) {
}
