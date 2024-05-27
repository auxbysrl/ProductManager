package com.auxby.productmanager.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileTest {
    @Test
    void testEquals() {
        var firstFile = new File();
        var secondFile = new File();
        assertEquals(firstFile, firstFile);
        assertNotEquals(firstFile, new Contact());
        assertNotEquals(firstFile, secondFile);

        firstFile.setId(1);
        secondFile.setId(1);
        assertEquals(firstFile, secondFile);

        secondFile.setId(2);
        assertNotEquals(firstFile, secondFile);
    }
}