package com.auxby.productmanager.api.v1.offer.repository;

import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "OFFER_DATA")
public class OfferData extends AuxbyBaseEntity {
    @Column(name = "name")
    private String key;
    private String value;
    private String fieldId;
    @ManyToOne(fetch = FetchType.LAZY)
    private Offer offer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferData)) return false;

        return getId() != null && getId().equals(((OfferData) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
