package com.auxby.productmanager.api.v1.offer.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull @Min(value = 0, message = "Number of coins must be positive.") Integer requiredCoins) {
}
