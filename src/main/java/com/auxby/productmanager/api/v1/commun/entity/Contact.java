package com.auxby.productmanager.api.v1.commun.entity;

import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.utils.enums.ContactType;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "CONTACT")
public class Contact extends AuxbyBaseEntity {
    @Enumerated(EnumType.STRING)
    private ContactType type;
    private String value;
    @ManyToOne(fetch = FetchType.LAZY)
    private Offer offer;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDetails user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;

        return getId() != null && getId().equals(((Contact) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
