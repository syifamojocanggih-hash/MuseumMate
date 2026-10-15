package com.museummate.model;

/**
 * Representasi tingkat kondisi fisik koleksi museum beserta bobot perhitungannya
 * sesuai spesifikasi PRD Bagian 6.4:
 * Baik = 1, Cukup = 2, Rusak Ringan = 3, Rusak Berat = 4.
 */
public enum KondisiKoleksi {
    BAIK(1, "Baik"),
    CUKUP(2, "Cukup"),
    RUSAK_RINGAN(3, "Rusak Ringan"),
    RUSAK_BERAT(4, "Rusak Berat");

    private final int bobot;
    private final String label;

    KondisiKoleksi(int bobot, String label) {
        this.bobot = bobot;
        this.label = label;
    }

    public int getBobot() {
        return bobot;
    }

    public String getLabel() {
        return label;
    }

    public static KondisiKoleksi fromString(String text) {
        if (text == null) return BAIK;
        for (KondisiKoleksi k : KondisiKoleksi.values()) {
            if (k.name().equalsIgnoreCase(text) || k.label.equalsIgnoreCase(text)) {
                return k;
            }
        }
        return BAIK;
    }

    @Override
    public String toString() {
        return label;
    }
}
