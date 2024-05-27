package com.auxby.productmanager.api.v1.offer.specification.criteria.filter;

public record DetailsFilter(
        String key,
        String value,
        Integer highestValue,
        Integer lowestValue
) {
}
