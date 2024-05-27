package com.auxby.productmanager.utils;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeProcessorTest {

    @Test
    void computeNumberOfDaysSincePublished() {
        Date dateInThePast = new GregorianCalendar(2020, Calendar.APRIL, 10).getTime();
        Date dateInTheFuture = new GregorianCalendar(2050, Calendar.APRIL, 10).getTime();
        var result = DateTimeProcessor.computeNumberOfDaysSincePublished(dateInThePast);
        var result2 = DateTimeProcessor.computeNumberOfDaysSincePublished(dateInTheFuture);
        assertTrue(result > 0);
        assertTrue(result2 > 0);
    }

    @Test
    void isDateInThePast() {
        Date dateInThePast = new GregorianCalendar(2020, Calendar.APRIL, 10).getTime();
        Date dateInTheFuture = new GregorianCalendar(2050, Calendar.APRIL, 10).getTime();
        var result = DateTimeProcessor.isDateInThePast(dateInThePast);
        var result2 = DateTimeProcessor.isDateInThePast(dateInTheFuture);
        assertTrue(result);
        assertFalse(result2);
    }
}