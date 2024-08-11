package com.auxby.productmanager.entity;

import com.auxby.productmanager.api.v1.commun.entity.Contact;
import com.auxby.productmanager.api.v1.user.repository.UserDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UserDetailsTest {
    @Test
    void testEquals() {
        var firstUser = new UserDetails();
        var secondUser = new UserDetails();
        assertEquals(firstUser, firstUser);
        assertNotEquals(firstUser, new Contact());
        assertNotEquals(firstUser, secondUser);

        firstUser.setId(1);
        secondUser.setId(1);
        assertEquals(firstUser, secondUser);

        secondUser.setId(2);
        assertNotEquals(firstUser, secondUser);
    }

    @Test
    void testHashCode() {
        var firstUser = new UserDetails();
        firstUser.setId(1);
        var secondUser = new UserDetails();
        secondUser.setId(2);
        assertEquals(firstUser.hashCode(), secondUser.hashCode());
    }
}