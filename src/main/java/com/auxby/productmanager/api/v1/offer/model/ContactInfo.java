package com.auxby.productmanager.api.v1.offer.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.auxby.productmanager.utils.constant.AppConstant.VALID_PHONE_REGEX;

public record ContactInfo(@Pattern(regexp = VALID_PHONE_REGEX, message = "Invalid phone number.") String phoneNumber,
                          @NotBlank(message = "Location must be provided.") String location) {
}
