package com.auxby.productmanager.api.v1.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    void deleteAllByOfferIdAndUserIdAndTypeIsIn(Integer offerId, Integer userId, List<String> types);
}
