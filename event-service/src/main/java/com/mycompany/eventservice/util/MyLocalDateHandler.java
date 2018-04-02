package com.mycompany.eventservice.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MyLocalDateHandler {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern(PATTERN);

    public static Date fromStringToDate(String string) {
        LocalDateTime ldt = LocalDateTime.parse(string, DTF);
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));
        return Date.from(zdt.toInstant());
    }

    public static String fromDateToString(Date date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
        return zdt.format(DTF);
    }

}
