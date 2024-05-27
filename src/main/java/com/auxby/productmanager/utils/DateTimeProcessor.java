package com.auxby.productmanager.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.auxby.productmanager.utils.constant.AppConstant.OFFER_VALID_NUMBER_OF_DAYS;

public class DateTimeProcessor {
    private DateTimeProcessor() {
    }

    /**
     * Calculate the number of days that have/had past
     *
     * @param publishDate - the past/future date
     * @return the number of days that have/had to past
     */
    public static long computeNumberOfDaysSincePublished(Date publishDate) {
        long dateInMs = publishDate.getTime();
        long currentDateInMs = System.currentTimeMillis();
        long timeDiff = Math.abs(currentDateInMs - dateInMs);

        return TimeUnit.DAYS.convert(timeDiff, TimeUnit.DAYS);
    }

    /**
     * @param date - the date
     * @return true if date is in the past
     */
    public static boolean isDateInThePast(Date date) {
        Date currentDate = new Date();

        return date.before(currentDate);
    }

    /**
     * @param lastActiveDate - the date the offer will expire
     * @return the last day the offer will be available on system
     */
    public static java.sql.Date computeOfferExpirationDate(Date lastActiveDate) {
        LocalDate localDate = lastActiveDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        int daysToBeKeptInSystem = OFFER_VALID_NUMBER_OF_DAYS * 2;

        return java.sql.Date.valueOf(localDate.plusDays(daysToBeKeptInSystem));
    }

    public static Date computeAuctionExpirationDate(Date expirationDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNullElseGet(expirationDate, () -> java.sql.Date.valueOf(LocalDate.now().plusMonths(1))));
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
