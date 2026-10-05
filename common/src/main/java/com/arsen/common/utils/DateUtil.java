package com.arsen.common.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
