package com.auxby.productmanager.entity;

import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ADDRESS")
public class Address extends AuxbyBaseEntity {
    private String city;
    private String country;
    private String street;
    @ManyToOne(fetch = FetchType.LAZY)
    private Offer offer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;

        return getId() != null && getId().equals(((Address) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
