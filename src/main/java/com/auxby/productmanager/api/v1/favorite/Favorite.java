package com.auxby.productmanager.api.v1.favorite;

import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "FAVORITE")
public class Favorite extends AuxbyBaseEntity {
    private Integer userId;
    private Integer offerId;
}
