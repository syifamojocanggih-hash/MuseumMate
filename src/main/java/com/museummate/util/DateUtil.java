package com.museummate.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilitas pemformatan tanggal dan waktu standar Indonesia.
 */
public class DateUtil {
    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", INDONESIA);
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", INDONESIA);

    public static String formatIndonesian(LocalDate date) {
        if (date == null) return "-";
        return date.format(DATE_FORMATTER);
    }

    public static String formatShort(LocalDate date) {
        if (date == null) return "-";
        return date.format(SHORT_DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DATETIME_FORMATTER);
    }
}
