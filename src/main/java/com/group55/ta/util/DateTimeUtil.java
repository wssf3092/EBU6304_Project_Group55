package com.group55.ta.util;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Date time helper methods.
 */
public final class DateTimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private DateTimeUtil() {
    }

    public static String nowIso() {
        return LocalDateTime.now().format(FORMATTER);
    }

    public static String formatDateTime(String isoValue) {
        if (ValidationUtil.isBlank(isoValue)) {
            return "Not available";
        }
        try {
            return LocalDateTime.parse(isoValue, FORMATTER).format(DISPLAY_DATE_TIME);
        } catch (Exception ex) {
            return isoValue;
        }
    }

    public static String formatDate(String dateValue) {
        if (ValidationUtil.isBlank(dateValue)) {
            return "Not available";
        }
        try {
            return LocalDate.parse(dateValue).format(DISPLAY_DATE);
        } catch (Exception ex) {
            return dateValue;
        }
    }

    public static boolean isPastDate(String dateValue) {
        if (ValidationUtil.isBlank(dateValue)) {
            return false;
        }
        try {
            return LocalDate.parse(dateValue).isBefore(LocalDate.now());
        } catch (Exception ex) {
            return false;
        }
    }
}
