package com.auxby.productmanager.entity;

import com.auxby.productmanager.api.v1.commun.entity.Address;
import com.auxby.productmanager.api.v1.commun.entity.Contact;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactTest {
    @Test
    void testEquals() {
        var contact1 = new Contact();
        var contact2 = new Contact();
        var contact3 = new Contact();

        contact1.setId(1);
        contact1.setValue("0722100600");
        contact2.setId(2);
        contact2.setValue("0722100500");
        contact3.setId(1);
        contact3.setValue("0722100400");

        assertSame(contact1.getId(), contact3.getId());
        assertEquals(contact1, contact3);
        assertNotEquals(contact1, contact2);
        assertNotEquals(contact1.getId(), contact2.getId());
        assertTrue(contact1.equals(contact1));
        assertFalse(contact1.equals(new Address()));
        assertFalse(contact1.equals(new Contact()));
        assertFalse(new Contact().equals(contact1));
    }
}