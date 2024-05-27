package com.auxby.productmanager.api.v1.offer.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

public record PromoteInfo(
        @Min(value = 0, message = "Required coins number must be positive.") Integer requiredCoins,
        @NotNull(message = "Promote expiration date must be provided.") Date expirationDate) {
}
