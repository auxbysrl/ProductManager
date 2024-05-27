package com.auxby.productmanager.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;

@Getter
public enum CurrencyType {
    RON("Ron") {
        @Override
        public BigDecimal toRonConversion(BigDecimal value) {
            if (Objects.isNull(value)) {
                return null;
            }
            return value;
        }
    },
    EURO("Euro") {
        @Override
        public BigDecimal toRonConversion(BigDecimal value) {
            if (Objects.isNull(value)) {
                return null;
            }
            return value.multiply(RON_EURO_CONVERSION);
        }
    };


    public static final BigDecimal RON_EURO_CONVERSION = BigDecimal.valueOf(5);
    private final String currency;

    CurrencyType(String currency) {
        this.currency = currency;
    }

    @JsonCreator
    public static CurrencyType getConditionType(String value) {
        return Arrays.stream(CurrencyType.values())
                .toList()
                .stream()
                .filter(v -> v.getCurrency().equals(value))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Condition type not found for value:" + value));
    }

    @Override
    public String toString() {
        return currency;
    }

    public abstract BigDecimal toRonConversion(BigDecimal value);
}
