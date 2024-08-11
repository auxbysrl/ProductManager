package com.auxby.productmanager.api.v1.commun.entity;

import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "FILES")
public class File extends AuxbyBaseEntity {
    private String url;
    private boolean isPrimary;
    @ManyToOne(fetch = FetchType.LAZY)
    private Offer offer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof File)) return false;

        return getId() != null && getId().equals(((File) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
