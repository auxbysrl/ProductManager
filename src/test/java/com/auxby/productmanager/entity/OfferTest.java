package com.auxby.productmanager.entity;

import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.commun.entity.Address;
import com.auxby.productmanager.api.v1.commun.entity.Contact;
import com.auxby.productmanager.api.v1.commun.entity.File;
import com.auxby.productmanager.api.v1.offer.repository.Offer;
import com.auxby.productmanager.api.v1.offer.repository.OfferData;
import com.auxby.productmanager.utils.enums.ConditionType;
import com.auxby.productmanager.utils.enums.ContactType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferTest {
    @Test
    void testOffer_contactActions() {
        var offer = new Offer();
        offer.setId(1);
        offer.setAvailable(true);
        offer.setCondition(ConditionType.NEW);

        var offerContact = new Contact();
        offerContact.setId(1);
        offerContact.setType(ContactType.PHONE);
        offerContact.setValue("0740200100");

        offer.addContact(offerContact);
        assertEquals(1, offer.getContacts().size());
        assertEquals(offerContact, offer.getContacts().stream().findFirst().get());

        offer.removeContact(offerContact);
        assertEquals(0, offer.getContacts().size());
    }

    @Test
    void testOffer_addressActions() {
        var offer = new Offer();
        offer.setId(1);
        offer.setAvailable(true);
        offer.setCondition(ConditionType.NEW);

        var addressContact = new Address();
        addressContact.setId(1);
        addressContact.setStreet("Street Test");

        offer.addAddress(addressContact);
        assertEquals(1, offer.getAddresses().size());
        assertEquals(addressContact, offer.getAddresses().stream().findFirst().get());

        offer.removeAddress(addressContact);
        assertEquals(0, offer.getAddresses().size());
    }

    @Test
    void testOffer_fileActions() {
        var offer = new Offer();
        offer.setId(1);
        offer.setAvailable(true);
        offer.setCondition(ConditionType.NEW);

        var file = new File();
        file.setId(1);

        offer.addFile(file);
        assertEquals(1, offer.getFiles().size());
        assertEquals(file, offer.getFiles().stream().findFirst().get());

        offer.removeFile(file);
        assertEquals(0, offer.getFiles().size());
    }

    @Test
    void testOffer_dataActions() {
        var offer = new Offer();
        offer.setId(1);
        offer.setAvailable(true);
        offer.setCondition(ConditionType.NEW);

        var data = new OfferData();
        data.setId(1);

        offer.addData(data);
        assertEquals(1, offer.getOfferDetails().size());
        assertEquals(data, offer.getOfferDetails().stream().findFirst().get());

        offer.removeData(data);
        assertEquals(0, offer.getOfferDetails().size());
    }

    @Test
    void testOffer_BidActions() {
        var offer = new Offer();
        offer.setId(1);
        offer.setAvailable(true);
        offer.setCondition(ConditionType.NEW);

        var data = new Bid();
        data.setId(1);

        offer.addBid(data);
        assertEquals(1, offer.getBids().size());
        assertEquals(data, offer.getBids().stream().findFirst().get());

        offer.removeBid(data);
        assertEquals(0, offer.getBids().size());
    }
}