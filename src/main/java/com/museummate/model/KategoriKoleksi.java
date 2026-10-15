package com.museummate.model;

/**
 * Kategori koleksi museum dan faktor risiko kerentanan
 * sesuai spesifikasi PRD Bagian 6.4:
 * Naskah = 1.5, Lukisan = 1.2, Artefak = 1.0, Fosil = 0.8.
 */
public enum KategoriKoleksi {
    NASKAH(1.5, "Naskah"),
    LUKISAN(1.2, "Lukisan"),
    ARTEFAK(1.0, "Artefak"),
    FOSIL(0.8, "Fosil");

    private final double faktor;
    private final String label;

    KategoriKoleksi(double faktor, String label) {
        this.faktor = faktor;
        this.label = label;
    }

    public double getFaktor() {
        return faktor;
    }

    public String getLabel() {
        return label;
    }

    public static KategoriKoleksi fromString(String text) {
        if (text == null) return ARTEFAK;
        for (KategoriKoleksi k : KategoriKoleksi.values()) {
            if (k.name().equalsIgnoreCase(text) || k.label.equalsIgnoreCase(text)) {
                return k;
            }
        }
        return ARTEFAK;
    }

    @Override
    public String toString() {
        return label;
    }
}
