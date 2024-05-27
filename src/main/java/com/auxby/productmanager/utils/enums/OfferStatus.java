package com.auxby.productmanager.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;

@Getter
public enum OfferStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    FINISHED("Finished"),
    INTERRUPTED("Interrupted");

    private final String status;

    OfferStatus(String status) {
        this.status = status;
    }

    @JsonCreator
    public static OfferStatus getConditionType(String value) {
        return Arrays.stream(OfferStatus.values())
                .toList()
                .stream()
                .filter(v -> v.getStatus().equals(value))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Condition type not found for value:" + value));
    }

    @Override
    public String toString() {
        return status;
    }
}
