package com.auxby.productmanager.api.v1.offer.model;

import com.auxby.productmanager.utils.enums.ConditionType;
import com.auxby.productmanager.utils.enums.CurrencyType;
import com.auxby.productmanager.utils.enums.OfferType;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public record OfferInfo(@NotBlank(message = "Title is mandatory.") String title,
                        @NotBlank(message = "Description is mandatory.") String description,
                        ConditionType conditionType,
                        @NotNull(message = "Category is mandatory.") Long categoryId,
                        OfferType offerType,
                        @NotNull(message = "Price is mandatory.")
                        @Min(value = 0, message = "Price must be positive.") BigDecimal price,
                        @NotNull(message = "Currency type is mandatory.") CurrencyType currencyType,
                        Timestamp auctionEndDate,
                        @Valid ContactInfo contactInfo,
                        List<CategoryDetails> categoryDetails,
                        @NotNull @Min(value = 0, message = "Number of coins must be positive.") Integer requiredCoins,
                        boolean autoExtend
) {
}
