package com.auxby.productmanager.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;

@Getter
public enum OfferType {
    AUCTION("Auction"),
    FIXED_PRICE("Fix price");

    private final String type;

    OfferType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static OfferType getConditionType(String value) {
        return Arrays.stream(OfferType.values())
                .toList()
                .stream()
                .filter(v -> v.getType().equals(value))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Offer type not found for value:" + value));
    }

    @Override
    public String toString() {
        return type;
    }
}
