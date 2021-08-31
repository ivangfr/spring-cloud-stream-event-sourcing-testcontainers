package com.mycompany.eventservice.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MyLocalDateHandler {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String ZONE_ID = "UTC";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern(PATTERN);

    private MyLocalDateHandler() {
    }

    public static Date fromStringToDate(String string) {
        LocalDateTime ldt = LocalDateTime.parse(string, DTF);
        ZonedDateTime zdt = ldt.atZone(ZoneId.of(ZONE_ID));
        return Date.from(zdt.toInstant());
    }

    public static String fromDateToString(Date date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(ZONE_ID));
        return zdt.format(DTF);
    }
}
