package com.sales.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtils {

    private DateUtils() {
    }

    public static long getStartOfMonthEpochMillis(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        return start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long getStartOfNextMonthEpochMillis(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0)
                .plusMonths(1);
        return start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
