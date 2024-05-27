package com.auxby.productmanager.api.v1.offer.model;

import java.util.Map;

public record SearchSummaryInfo(Map<Long, Integer> categoryResult) {
}
