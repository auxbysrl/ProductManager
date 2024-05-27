package com.auxby.productmanager.api.v1.offer.specification.criteria.filter;

import com.auxby.productmanager.utils.enums.CurrencyType;

import java.math.BigDecimal;

public record PriceFilter(BigDecimal highestPrice,
                          BigDecimal lowestPrice,
                          CurrencyType currencyType
) {
}
