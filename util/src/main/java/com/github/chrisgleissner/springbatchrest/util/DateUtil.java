package com.github.chrisgleissner.springbatchrest.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {

    public static LocalDateTime localDateTime(Date date) {
        return date == null ? null : date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
