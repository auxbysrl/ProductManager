package com.auxby.productmanager.api.v1.bid.model;

import java.util.Set;

public record PlaceBidResponse(boolean wasBidAccepted,
                               Set<BidSummary> offerBids
) {

}
