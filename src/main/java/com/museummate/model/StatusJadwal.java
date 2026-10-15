package com.museummate.model;

/**
 * Status siklus jadwal perawatan koleksi museum.
 */
public enum StatusJadwal {
    DIRENCANAKAN("Direncanakan"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusJadwal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static StatusJadwal fromString(String text) {
        if (text == null) return DIRENCANAKAN;
        for (StatusJadwal s : StatusJadwal.values()) {
            if (s.name().equalsIgnoreCase(text) || s.label.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return DIRENCANAKAN;
    }

    @Override
    public String toString() {
        return label;
    }
}
