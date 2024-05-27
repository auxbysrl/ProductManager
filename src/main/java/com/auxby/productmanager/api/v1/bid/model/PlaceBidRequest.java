package com.auxby.productmanager.api.v1.bid.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PlaceBidRequest(@NotNull Integer offerId,
                              @NotNull @Min(value = 0, message = "Amount must be positive.") BigDecimal amount,
                              @NotNull @Min(value = 0, message = "Number of coins must be positive.") Integer requiredCoins) {
}
