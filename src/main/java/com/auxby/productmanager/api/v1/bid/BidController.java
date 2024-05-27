package com.auxby.productmanager.api.v1.bid;

import com.auxby.productmanager.api.v1.bid.model.PlaceBidRequest;
import com.auxby.productmanager.api.v1.bid.model.PlaceBidResponse;
import com.auxby.productmanager.api.v1.offer.model.OfferSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.auxby.productmanager.utils.constant.AppConstant.BASE_V1_URL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(BASE_V1_URL)
@Tag(name = "Offers Bids APIs", description = "REST - endpoints for Bids.")
public class BidController {

    private final BidService bidService;

    @PostMapping("/bid")
    @Operation(summary = "Place a new bid for an offer.")
    public PlaceBidResponse createOfferBid(@Valid @RequestBody PlaceBidRequest placeBidRequest) {
        log.debug("POST - add a new bid for offer with key:" + placeBidRequest.offerId());
        return bidService.createBidForOnAuctionOffer(placeBidRequest);
    }

    @GetMapping("/my-bids/offers")
    @Operation(summary = "Get all user bids.")
    public List<OfferSummary> getUserBidOffers() {
        log.debug("GET - all user bids offers.");
        return bidService.getUserBids();
    }
}
