package com.team1.moim.global.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeConverter {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a hh시 mm분");

    public static String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime.format(dateTimeFormatter);
    }
}
