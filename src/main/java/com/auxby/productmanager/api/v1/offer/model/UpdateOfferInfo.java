package com.auxby.productmanager.api.v1.offer.model;

import com.auxby.productmanager.utils.enums.ConditionType;
import com.auxby.productmanager.utils.enums.CurrencyType;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record UpdateOfferInfo(@NotBlank(message = "Title is mandatory.") String title,
                              @Length(max = 9000, message = "Max length exceeded.")
                              @NotBlank(message = "Description is mandatory.") String description,
                              ConditionType conditionType,
                              @NotNull(message = "Price is mandatory.")
                              @Min(value = 0, message = "Price must be positive.") BigDecimal price,
                              @NotNull(message = "Currency type is mandatory.") CurrencyType currencyType,
                              @Valid ContactInfo contactInfo,
                              List<CategoryDetails> categoryDetails) {
}
