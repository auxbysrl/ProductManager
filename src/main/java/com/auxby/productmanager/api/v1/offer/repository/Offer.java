package com.auxby.productmanager.api.v1.offer.repository;

import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.commun.entity.Address;
import com.auxby.productmanager.api.v1.commun.entity.Contact;
import com.auxby.productmanager.api.v1.commun.entity.File;
import com.auxby.productmanager.api.v1.commun.system_configuration.SystemConfiguration;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import com.auxby.productmanager.api.v1.commun.entity.base.AuxbyBaseEntity;
import com.auxby.productmanager.utils.enums.ConditionType;
import com.auxby.productmanager.utils.enums.ContactType;
import com.auxby.productmanager.utils.enums.CurrencyType;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Data
@Entity
@Table(name = "OFFER")
@NamedEntityGraph(name = "offer-user-graph",
        attributeNodes = @NamedAttributeNode(value = "owner"))
@NamedEntityGraph(name = "offer-contacts-graph",
        attributeNodes = @NamedAttributeNode(value = "contacts"))
@NamedEntityGraph(name = "offer-addresses-graph",
        attributeNodes = @NamedAttributeNode(value = "addresses"))
@NamedEntityGraph(name = "offer-details-graph",
        attributeNodes = @NamedAttributeNode(value = "offerDetails"))
@NamedEntityGraph(name = "offer-files-graph",
        attributeNodes = @NamedAttributeNode(value = "files"))
@NamedEntityGraph(name = "offer-bids-graph",
        attributeNodes = @NamedAttributeNode(value = "bids"))
public class Offer extends AuxbyBaseEntity {
    private String name;
    private String description;
    @Column(name = "`condition`")
    @Enumerated(EnumType.STRING)
    private ConditionType condition;
    private Long categoryId;
    private Date publishDate;
    private BigDecimal price;
    private boolean isOnAuction;
    private boolean isAvailable;
    private Date auctionStartDate;
    private Date auctionEndDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private UserDetails owner;
    private Date expirationDate;
    private String currencyType;
    private Integer viewsNumber;
    @Formula("(SELECT Count(i.id) FROM favorite i WHERE i.offer_id = id)")
    private Integer setAsFavoriteNumber;
    @Column(name = "promote")
    private boolean isPromoted;
    private boolean autoExtend;
    private Integer coinsToExtend;
    private Date promoteExpirationDate;
    @Column(name = "auction_winner")
    private Integer auctionWinner;
    private String deepLink;

    @ToString.Exclude
    @OneToMany(mappedBy = "offer",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Contact> contacts = new HashSet<>();
    @ToString.Exclude
    @OneToMany(mappedBy = "offer",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Address> addresses = new HashSet<>();
    @ToString.Exclude
    @OneToMany(mappedBy = "offer",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<OfferData> offerDetails = new HashSet<>();
    @ToString.Exclude
    @OneToMany(mappedBy = "offer",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<File> files = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "offer",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Bid> bids = new HashSet<>();

    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setOffer(this);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setOffer(null);
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setOffer(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setOffer(null);
    }

    public void addData(OfferData data) {
        offerDetails.add(data);
        data.setOffer(this);
    }

    public void removeData(OfferData data) {
        offerDetails.remove(data);
        data.setOffer(null);
    }

    public void addFile(File file) {
        files.add(file);
        file.setOffer(this);
    }

    public void removeFile(File file) {
        files.remove(file);
        file.setOffer(null);
    }

    public void addBid(Bid bid) {
        bids.add(bid);
        bid.setOffer(this);
    }

    public void removeBid(Bid bid) {
        bids.remove(bid);
        bid.setOffer(null);
    }

    public void increaseViewNumber() {
        if (Objects.isNull(viewsNumber)) {
            this.viewsNumber = 0;
        } else {
            this.viewsNumber += 1;
        }
    }

    public String getPhoneNumbersAsString() {
        return contacts.stream()
                .filter(c -> c.getType() == ContactType.PHONE)
                .map(Contact::getValue)
                .findFirst()
                .orElse("");
    }

    public void closeAuction() {
        if (isOnAuction && !bids.isEmpty()) {
            this.isAvailable = false;
            var winner = bids.stream()
                    .max(Comparator.comparing(Bid::getBidValue));
            winner.ifPresent(w -> {
                w.setIsWinner(true);
                this.auctionWinner = winner.get().getOwner().getId();
            });
            // Return coins for auction losers
            bids.stream()
                    .filter(b -> Objects.isNull(b.getIsWinner()) || !b.getIsWinner())
                    .forEach(bid -> {
                        bid.setIsWinner(false);
                        if (bid.getChargedCoins() != null) {
                            bid.getOwner().setAvailableCoins(bid.getOwner().getAvailableCoins() + bid.getChargedCoins());
                        }
                    });
        }
    }

    public void computeDBPrice(BigDecimal postPrice, SystemConfiguration configuration) {
        // In DB all values for price are in RON
        CurrencyType type = CurrencyType.getCurrencyType(this.currencyType);
        this.price = type.toRonConversion(postPrice, configuration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Offer)) return false;

        return getId() != null && getId().equals(((Offer) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Optional<Bid> getWinner() {
        return bids.stream()
                .filter(bid -> bid.getIsWinner() != null && bid.getIsWinner())
                .findFirst();
    }

    public Optional<File> getMainImage() {
        return files.stream()
                .filter(File::isPrimary)
                .findFirst();
    }
}
