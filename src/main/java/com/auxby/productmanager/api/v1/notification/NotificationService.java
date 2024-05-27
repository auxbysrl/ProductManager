package com.auxby.productmanager.api.v1.notification;


import com.auxby.productmanager.api.v1.notification.repository.NotificationRepository;
import com.auxby.productmanager.rabbitmq.message.MessageType;
import com.auxby.productmanager.utils.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public void deleteOfferNotifications(Integer offerId) {
        var notificationTypesForDelete = List.of(
                MessageType.AUCTION_INTERRUPTED.name(), MessageType.AUCTION_ENDED.name(),
                MessageType.BID_EXCEEDED.name(), MessageType.AUCTION_WON.name()
        );
        Integer userId = SecurityContextUtil.getAuthenticatedUser().getId();
            repository.deleteAllByOfferIdAndUserIdAndTypeIsIn(offerId, userId, notificationTypesForDelete);
    }
}
