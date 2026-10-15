package com.museummate.model;

/**
 * Hak akses dan peran pengguna sistem MuseumMate (Admin dan Konservator).
 */
public enum PeranPengguna {
    ADMIN("Administrator"),
    KONSERVATOR("Konservator");

    private final String label;

    PeranPengguna(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PeranPengguna fromString(String text) {
        if (text == null) return KONSERVATOR;
        for (PeranPengguna p : PeranPengguna.values()) {
            if (p.name().equalsIgnoreCase(text) || p.label.equalsIgnoreCase(text)) {
                return p;
            }
        }
        return KONSERVATOR;
    }

    @Override
    public String toString() {
        return label;
    }
}
