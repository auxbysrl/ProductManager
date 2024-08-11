package com.auxby.productmanager.entity;

import com.auxby.productmanager.api.v1.commun.entity.Contact;
import com.auxby.productmanager.api.v1.offer.repository.OfferData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferDataTest {
    @Test
    void testEquals() {
        var firstOffer = new OfferData();
        var secondOffer = new OfferData();
        assertEquals(firstOffer, firstOffer);
        assertNotEquals(firstOffer, new Contact());
        assertNotEquals(firstOffer, secondOffer);

        firstOffer.setId(1);
        secondOffer.setId(1);
        assertEquals(firstOffer, secondOffer);

        secondOffer.setId(2);
        assertNotEquals(firstOffer, secondOffer);
    }

    @Test
    void testHashCode() {
        var firstOffer = new OfferData();
        firstOffer.setId(1);
        var secondOffer = new OfferData();
        secondOffer.setId(2);
        assertEquals(firstOffer.hashCode(), secondOffer.hashCode());
    }
}