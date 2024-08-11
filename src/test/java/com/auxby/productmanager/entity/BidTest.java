package com.auxby.productmanager.entity;

import com.auxby.productmanager.api.v1.bid.repository.Bid;
import com.auxby.productmanager.api.v1.commun.entity.File;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidTest {

    @Test
    void testEquals() {
        Bid firstBid = new Bid();
        Bid secondBid = new Bid();
        firstBid.setId(1);
        firstBid.setBidValue(BigDecimal.TEN);
        secondBid.setId(1);
        secondBid.setBidValue(BigDecimal.TEN);
        assertEquals(firstBid, secondBid);
        secondBid.setId(2);
        assertNotEquals(firstBid, secondBid);
        assertNotEquals(firstBid, new File());
        secondBid.setId(null);
        assertNotEquals(firstBid, secondBid);
    }
}