package com.ivanfranchin.eventservice.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MyLocalDateHandler {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final ZoneId ZONE_ID = ZoneId.of("UTC");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern(PATTERN);

    private MyLocalDateHandler() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String fromDateToString(Date date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID);
        return zdt.format(DTF);
    }
}
