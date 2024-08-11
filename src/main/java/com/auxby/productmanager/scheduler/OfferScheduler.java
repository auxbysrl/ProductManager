package com.auxby.productmanager.scheduler;

import com.auxby.productmanager.api.v1.bid.repository.BidRepository;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.offer.repository.OfferRepository;
import com.auxby.productmanager.api.v1.offer.repository.OfferSummaryProjection;
import com.auxby.productmanager.api.v1.user.UserDetailsRepository;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.auxby.productmanager.rabbitmq.message.MessageParams.*;
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
    private static final String EVERY_DAY_CLOSE_AUCTION_CRON = "0 1 13 * * ?";
    private static final String EVERY_DAY_NOTIFY_OWNER_CRON = "0 0 10 * * ?";

    @Scheduled(cron = EVERY_DAY_ON_MIDNIGHT_UPDATE_CRON)
    @Transactional
    public void updateOffersTask() {
        log.info("--------- Update offers availability. ------------");
        LocalDate today = LocalDate.now();
        ZonedDateTime thirtyDaysAgo = today.minusDays(30).atStartOfDay(ZoneId.systemDefault());
        offerRepository.updateExpiredOffers(Date.from(thirtyDaysAgo.toInstant()));

        log.info("--------- Update promotion status. ---------------");
        offerRepository.updateExpiredPromotions();

        log.info("--------- Update offers task finished. -----------");
    }

    @Scheduled(cron = EVERY_DAY_ON_MIDNIGHT_DELETE_CRON)
    @Transactional
    public void deleteOffersTask() {
        //TODO: improve performance, it will take a long period of time to delete each offer with her children
        log.info("--------- Delete offers task started. --------------");
        List<Offer> listOfOffersToDelete = offerRepository.findAllExpiredOffers();
        if (!listOfOffersToDelete.isEmpty()) {
            listOfOffersToDelete.forEach(offer -> {
                offerRepository.delete(offer);
                awsClientService.deleteOfferResources(offer.getOwner().getUsername(), offer.getId());
            });
        }
        log.info("--------- Delete offers task finished. %s offers were deleted. ---------".formatted(listOfOffersToDelete.size()));
    }

    @Scheduled(cron = EVERY_DAY_ON_MIDNIGHT_EXTEND_CRON)
    @Transactional
    public void autoExtendOffersTask() {
        log.info("------- Auto extend offers task started. -------");

        LocalDate today = LocalDate.now();
        ZonedDateTime startOfDay = today.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime startOfNextDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        Date startDate = Date.from(startOfDay.toInstant());
        Date endDate = Date.from(startOfNextDay.toInstant());

        List<Offer> listOfOffersToAutoExtend = offerRepository.getOffersToAutoExtend(startDate, endDate);
        log.info(String.format("%s Offers to auto extend found.", listOfOffersToAutoExtend.size()));
        listOfOffersToAutoExtend.forEach(this::autoExtendOffer);

        log.info("------- Auto extend offers task finished. ----------");
    }

    @Scheduled(cron = EVERY_DAY_CLOSE_AUCTION_CRON)
    @Transactional
    public void setAuctionWinners() {
        // Set auction winners
        log.info("-------- Set auction winners. --------------");
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

        log.info("------- Finished setting the auction winners. ---------");
    }

    @Scheduled(cron = EVERY_DAY_NOTIFY_OWNER_CRON)
    @Transactional(readOnly = true)
    public void notifyOwners() {
        log.info("-------- Notify owners about expiration date. -----------");
        LocalDate today = LocalDate.now();
        Date publishStartDate = toDate(today.minusDays(30));
        Date publishEndDate = toDate(today.minusDays(27));
        Date auctionStartDate = toDate(today);
        Date auctionEndDate = toDate(today.plusDays(3));

        List<OfferSummaryProjection> offers = offerRepository.findAvailableOffersWithinDateRanges(publishStartDate, publishEndDate, auctionStartDate, auctionEndDate);
        offers.forEach(offer -> messageSender.send(MessagePayload.builder()
                .offerId(offer.getId())
                .receiver(offer.getOwner())
                .messageType(MessageType.OFFER_EXPIRATION)
                .messageExtraInfo(Map.of(OFFER_NAME.name(), offer.getName()))
                .build()));
        log.info("-------- Finished sending owners notification. ----------");
    }

    @Transactional
    public void triggerTaskOnDemand() {
        // Order of actions is important
        autoExtendOffersTask();
        deleteOffersTask();
        setAuctionWinners();
        updateOffersTask();
        notifyOwners();
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
        var extraInfo = new HashMap<String, String>();
        extraInfo.put(OFFER_NAME.name(), auction.getName());
        extraInfo.put(OFFER_ID.name(), String.valueOf(auction.getId()));

        auction.getWinner()
                .ifPresent(bid -> messageSender.send(MessagePayload.builder()
                        .offerId(auction.getId())
                        .messageType(MessageType.AUCTION_WON)
                        .receiver(bid.getOwner().getUsername())
                        .messageExtraInfo(extraInfo)
                        .build()));

        messageSender.send(MessagePayload.builder()
                .offerId(auction.getId())
                .messageType(MessageType.AUCTION_ENDED)
                .receiver(auction.getOwner().getUsername())
                .messageExtraInfo(extraInfo)
                .build());
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
