package com.auxby.productmanager.api.v1.commun.system_configuration;

import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "system_configuration")
public class SystemConfiguration extends AuxbyBaseEntity {
    private String name;
    private String value;
    private String code;
    private String description;
    private Timestamp expirationDate;
}
