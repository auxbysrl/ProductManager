package com.auxby.productmanager.api.v1.bid;

import com.auxby.productmanager.api.v1.bid.model.BidSummary;
import com.auxby.productmanager.api.v1.bid.model.PlaceBidRequest;
import com.auxby.productmanager.api.v1.bid.model.PlaceBidResponse;
import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.bid.repository.BidRepository;
import com.auxby.productmanager.api.v1.offer.OfferService;
import com.auxby.productmanager.api.v1.offer.model.OfferSummary;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.user.UserService;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.exception.ActionNotAllowException;
import com.auxby.productmanager.exception.BidDeclinedException;
import com.auxby.productmanager.exception.InsufficientCoinsException;
import com.auxby.productmanager.rabbitmq.MessageSender;
import com.auxby.productmanager.rabbitmq.message.MessagePayload;
import com.auxby.productmanager.rabbitmq.message.MessageType;
import com.auxby.productmanager.utils.DateTimeProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static com.auxby.productmanager.rabbitmq.message.MessageParams.OFFER_NAME;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    private final UserService userService;
    private final OfferService offerService;
    private final BidRepository bidRepository;
    private final MessageSender messageSender;

    @Transactional
    public PlaceBidResponse createBidForOnAuctionOffer(PlaceBidRequest placeBidRequest) {
        UserDetails user = userService.getUser()
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        Offer offer = offerService.findAvailableOnAuctionOfferById(placeBidRequest.offerId());
        if (DateTimeProcessor.isDateInThePast(offer.getAuctionEndDate())) {
            throw new ActionNotAllowException("Bid no longer allowed for this auction.");
        }

        Optional<Bid> biggestBid = offer.getBids()
                .stream()
                .max(Comparator.comparing(Bid::getBidValue));
        validateBid(offer, placeBidRequest, user, biggestBid);
        chargeCoins(user, placeBidRequest.requiredCoins());
        offer.addBid(getOfferBid(placeBidRequest, user, offer));

        if (biggestBid.isPresent()
                && !biggestBid.get().getOwner().getId().equals(user.getId())) {
            sendNotificationMessage(offer, biggestBid.get());
        }

        return new PlaceBidResponse(true, convertToBidSummaryList(offer.getBids()));
    }

    public List<OfferSummary> getUserBids() {
        UserDetails user = userService.getUser()
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        List<Integer> userFavorites = userService.getUserFavoriteOffersIds(user.getUsername());

        return bidRepository.getUserBids(user.getId())
                .stream()
                .map(Bid::getOffer)
                .distinct()
                .toList()
                .stream()
                .map(offer -> offerService.mapOfferToSummary(offer, userFavorites))
                .toList();
    }

    private void sendNotificationMessage(Offer offer,
                                         Bid biggestBid) {
        var message = MessagePayload.builder()
                .receiver(biggestBid.getOwner().getUsername())
                .offerId(offer.getId())
                .messageExtraInfo(Map.of(OFFER_NAME.name(), offer.getName()))
                .messageType(MessageType.BID_EXCEEDED)
                .build();
        messageSender.send(message);
    }

    private void validateBid(Offer offer,
                             PlaceBidRequest placeBidRequest,
                             UserDetails user,
                             Optional<Bid> biggestBid) {
        if (offer.getOwner().getUsername().equals(user.getUsername())) {
            throw new ActionNotAllowException("Bidding at owned offers is not allowed.");
        }
        var price = offerService.computePrice(offer);
        if (price.compareTo(placeBidRequest.amount()) > 0) {
            throw new ActionNotAllowException("Bid amount cannot be lower than offer price.");
        }
        if (biggestBid.isPresent()
                && (biggestBid.get().getBidValue().compareTo(placeBidRequest.amount()) > -1)) {
            throw new BidDeclinedException("Bid not accepted. A higher bid already in place.", convertToBidSummaryList(offer.getBids()));
        }
    }

    private Bid getOfferBid(PlaceBidRequest placeBidRequest, UserDetails user, Offer offer) {
        Bid bid = new Bid();
        bid.setOwner(user);
        bid.setOffer(offer);
        bid.setDate(new Date());
        bid.setBidValue(placeBidRequest.amount());
        bid.setChargedCoins(placeBidRequest.requiredCoins());

        return bid;
    }

    private Set<BidSummary> convertToBidSummaryList(Set<Bid> bids) {
        return bids.stream()
                .map(bid -> new BidSummary(bid.getOwner().getUsername(), bid.getBidValue()))
                .collect(Collectors.toSet());
    }

    private void chargeCoins(UserDetails userDetails, Integer coinsNeeded) {
        if (userDetails.getAvailableCoins() < coinsNeeded) {
            throw new InsufficientCoinsException("Place a new bid.");
        }
        Integer remainingCoins = userDetails.getAvailableCoins() - coinsNeeded;
        userService.updateUserCoinsNumber(userDetails.getUsername(), remainingCoins);
    }
}
