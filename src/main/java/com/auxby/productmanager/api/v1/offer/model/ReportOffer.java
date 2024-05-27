package com.auxby.productmanager.api.v1.offer.model;

import javax.validation.constraints.NotBlank;

public record ReportOffer(@NotBlank String type, String comment) {
}
