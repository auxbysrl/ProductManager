package com.auxby.productmanager.api.v1.notification.repository;


import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;

@Data
@Entity
@Table(name = "Notification")
public class Notification extends AuxbyBaseEntity {
    @Column(nullable = false)
    private Date date;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String message;
    @Column(name = "offer_id", nullable = false)
    private Integer offerId;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "room_id")
    private Integer roomId;
}
