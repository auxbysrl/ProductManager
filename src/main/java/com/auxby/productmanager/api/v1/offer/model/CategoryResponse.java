package com.auxby.productmanager.api.v1.offer.model;

import java.util.List;

public record CategoryResponse(Long id, String icon, List<Localization> label, Integer noOffers) {

}
