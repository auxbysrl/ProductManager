package com.auxby.productmanager.api.v1.bid.repository;

import com.amazonaws.util.StringUtils;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "BID")
public class Bid extends AuxbyBaseEntity {

    private Date date;
    @Column(name = "price")
    private BigDecimal bidValue;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserDetails owner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", referencedColumnName = "id")
    private Offer offer;
    @Column(name = "charged_coins")
    private Integer chargedCoins;
    @Column(name = "is_winner")
    private Boolean isWinner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bid)) return false;

        return getId() != null && getId().equals(((Bid) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public String getBidderName() {
        StringBuffer bidderName = new StringBuffer();
        if (StringUtils.hasValue(owner.getFirstName())) {
            bidderName.append(owner.getFirstName() + " ");
        }
        if (StringUtils.hasValue(owner.getLastName())) {
            bidderName.append(owner.getLastName());
        }
        if (bidderName.isEmpty()) {
            return owner.getUsername();
        }

        return bidderName.toString();
    }
}
