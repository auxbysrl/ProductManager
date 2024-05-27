package com.auxby.productmanager.scheduler;

import com.auxby.productmanager.api.v1.bid.BidRepository;
import com.auxby.productmanager.api.v1.offer.repository.OfferRepository;
import com.auxby.productmanager.api.v1.user.UserDetailsRepository;
import com.auxby.productmanager.entity.Bid;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.rabbitmq.MessageSender;
import com.auxby.productmanager.rabbitmq.message.MessagePayload;
import com.auxby.productmanager.rabbitmq.message.MessageType;
import com.auxby.productmanager.utils.service.AmazonClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.auxby.productmanager.rabbitmq.message.MessageParams.ACTION;
import static com.auxby.productmanager.rabbitmq.message.MessageParams.OFFER_NAME;
import static com.auxby.productmanager.utils.DateTimeProcessor.computeOfferExpirationDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferScheduler {

    private final BidRepository bidRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final OfferRepository offerRepository;
    private final AmazonClientService awsClientService;
    private final MessageSender messageSender;
    private static final String EVERY_DAY_ON_MIDNIGHT_EXTEND_CRON = "0 0 0 * * ?";
    private static final String EVERY_DAY_ON_MIDNIGHT_UPDATE_CRON = "5 0 0 * * ?";
    private static final String EVERY_DAY_ON_MIDNIGHT_DELETE_CRON = "10 0 0 * * ?";
    private static final String EVERY_DAY_CLOSE_AUCTION_CRON = "0 13 0 * * ?";

    @Scheduled(cron = EVERY_DAY_ON_MIDNIGHT_UPDATE_CRON)
    @Transactional
    public void updateOffersTask() {
        log.info("Update offers task started.");
        offerRepository.updateExpiredOffers();
        offerRepository.updateExpiredPromotions();
        log.info("Update offers task finished.");
    }

    @Scheduled(cron = EVERY_DAY_ON_MIDNIGHT_DELETE_CRON)
    @Transactional
    public void deleteOffersTask() {
        //TODO: improve performance, it will take a long period of time to delete each offer with her children
        log.info("Delete offers task started.");
        List<Offer> listOfOffersToDelete = offerRepository.findAllExpiredOffers();
        if (!listOfOffersToDelete.isEmpty()) {
            listOfOffersToDelete.forEach(offer -> {
                offerRepository.delete(offer);
                awsClientService.deleteOfferResources(offer.getOwner().getUsername(), offer.getId());
            });
        }
        log.info(String.format("Delete offers task finished. %s offers were deleted.", listOfOffersToDelete.size()));
    }

    @Scheduled(cron = EVERY_DAY_ON_MIDNIGHT_EXTEND_CRON)
    @Transactional
    public void autoExtendOffersTask() {
        log.info("Auto extend offers task started.");
        List<Offer> listOfOffersToAutoExtend = offerRepository.getOffersToAutoExtend();
        log.info(String.format("%s Offers to auto extend found.", listOfOffersToAutoExtend.size()));
        listOfOffersToAutoExtend.forEach(this::autoExtendOffer);

        log.info("Auto extend offers task finished.");
    }

    @Scheduled(cron = EVERY_DAY_CLOSE_AUCTION_CRON)
    @Transactional
    public void setAuctionWinners() {
        // Set auction winners
        log.info("Set auction winners.");
        List<Offer> auctions = offerRepository.getAuctionsToClose();
        auctions.forEach(auction -> {
            auction.closeAuction();
            bidRepository.saveAll(auction.getBids());
            try {
                notifyInvolvedEntities(auction);
            } catch (Exception e) {
                log.info("Failed to send notifications.");
            }
        });
    }

    @Transactional
    public void triggerTaskOnDemand() {
        // Order of actions is important
        autoExtendOffersTask();
        deleteOffersTask();
        setAuctionWinners();
        updateOffersTask();
    }

    private void autoExtendOffer(Offer offer) {
        if (offer.getOwner().getAvailableCoins() < offer.getCoinsToExtend()) {
            messageSender.send(MessagePayload.builder()
                    .offerId(offer.getId())
                    .receiver(offer.getOwner().getUsername())
                    .messageType(MessageType.ACTION_FAILED)
                    .messageExtraInfo(Map.of(ACTION.name(), "auto extend for offer %s.".formatted(offer.getName())))
                    .build());
            offer.setAutoExtend(false);
        } else {
            UserDetails owner = offer.getOwner();
            Integer remainingCoins = owner.getAvailableCoins() - offer.getCoinsToExtend();
            userDetailsRepository.updateUserCoinsNumber(offer.getOwner().getUsername(), remainingCoins);
            Date publishDate = new Date();
            offer.setPublishDate(publishDate);
            offer.setExpirationDate(computeOfferExpirationDate(publishDate));
        }
    }

    private void notifyInvolvedEntities(Offer auction) {
        auction.getBids()
                .stream()
                .filter(Bid::getIsWinner)
                .findFirst()
                .ifPresent(bid -> messageSender.send(MessagePayload.builder()
                        .offerId(auction.getId())
                        .messageType(MessageType.AUCTION_WON)
                        .receiver(bid.getOwner().getUsername())
                        .messageExtraInfo(Map.of(OFFER_NAME.name(), auction.getName()))
                        .build()));
        messageSender.send(MessagePayload.builder()
                .offerId(auction.getId())
                .messageType(MessageType.AUCTION_ENDED)
                .receiver(auction.getOwner().getUsername())
                .messageExtraInfo(Map.of(OFFER_NAME.name(), auction.getName()))
                .build());
    }
}
