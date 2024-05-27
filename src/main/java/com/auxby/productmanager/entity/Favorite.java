package com.auxby.productmanager.entity;

import com.auxby.productmanager.entity.base.AuxbyBaseEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "FAVORITE")
public class Favorite extends AuxbyBaseEntity {
    private Integer userId;
    private Integer offerId;
}
