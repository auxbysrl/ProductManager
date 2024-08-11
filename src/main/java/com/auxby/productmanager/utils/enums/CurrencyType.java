package com.auxby.productmanager.utils.enums;

import com.auxby.productmanager.api.v1.commun.system_configuration.SystemConfiguration;
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
        public String getSymbol() {
            return "lei";
        }
    },
    EURO("Euro") {
        @Override
        public String getSymbol() {
            return "€";
        }
    },
    USD("Usd") {
        @Override
        public String getSymbol() {
            return "$";
        }
    };

    private final String currency;

    CurrencyType(String currency) {
        this.currency = currency;
    }

    @JsonCreator
    public static CurrencyType getCurrencyType(String value) {
        return Arrays.stream(CurrencyType.values())
                .toList()
                .stream()
                .filter(v -> v.getCurrency().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Condition type not found for value:" + value));
    }

    @Override
    public String toString() {
        return currency;
    }

    public BigDecimal toRonConversion(BigDecimal value, SystemConfiguration configuration) {
        if (Objects.isNull(value)) {
            return null;
        }
        BigDecimal rate = BigDecimal.valueOf(Double.parseDouble(configuration.getValue()));
        return value.multiply(rate).setScale(4, RoundingMode.UP);
    }

    public BigDecimal getCurrencyPrice(BigDecimal value, SystemConfiguration configuration) {
        if (Objects.isNull(value)) {
            return null;
        }
        BigDecimal rate = BigDecimal.valueOf(Double.parseDouble(configuration.getValue()));
        return value.divide(rate, RoundingMode.UP).setScale(4, RoundingMode.UP);
    }

    public abstract String getSymbol();
}
