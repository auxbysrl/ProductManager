package com.auxby.productmanager.api.v1.user.repository;

import com.auxby.productmanager.api.v1.commun.entity.Contact;
import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "USER_DETAILS")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class)
})
public class UserDetails extends AuxbyBaseEntity {
    private String gender;
    private String lastName;
    private String firstName;
    private Integer userId;
    private String username;
    private String avatarUrl;
    private Integer availableCoins;
    private Date lastSeen;
    @Type(type = "json")
    @Column(name = "ratings", columnDefinition = "TEXT")
    private List<UserRating> ratings = new ArrayList<>();

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

    public Integer getUserRating() {
        if (ratings.isEmpty())
            return 5;
        return ratings.stream()
                .mapToInt(UserRating::rate)
                .sum() / ratings.size();
    }
}
