package com.auxby.productmanager.api.v1.user.repository;

import com.auxby.productmanager.entity.Contact;
import com.auxby.productmanager.entity.base.AuxbyBaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "USER_DETAILS")
public class UserDetails extends AuxbyBaseEntity {
    private String gender;
    private String lastName;
    private String firstName;
    private Integer userId;
    private String username;
    private String avatarUrl;
    private Integer availableCoins;
    private Date lastSeen;

    @ToString.Exclude
    @OneToMany(mappedBy = "user",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Contact> contacts = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetails)) return false;

        return getId() != null && getId().equals(((UserDetails) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
