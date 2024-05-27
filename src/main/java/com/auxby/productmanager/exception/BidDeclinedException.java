package com.auxby.productmanager.exception;

import com.auxby.productmanager.api.v1.bid.model.BidSummary;
import lombok.Getter;

import java.util.Set;

@Getter
public class BidDeclinedException extends RuntimeException {

    private final Set<BidSummary> offerBidsInPlace;

    public BidDeclinedException(String message, Set<BidSummary> bidsInPlace) {
        super(message);
        this.offerBidsInPlace = bidsInPlace;
    }
}
