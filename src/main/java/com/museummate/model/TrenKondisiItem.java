package com.museummate.model;

/**
 * Data Transfer Object (DTO) untuk merepresentasikan titik data tren kondisi koleksi (PRD F9 / UC-12).
 */
public class TrenKondisiItem {
    private String periode;       // format YYYY-MM untuk sorting
    private String labelPeriode;  // format "Mei 2026", "Jun 2026" untuk sumbu X
    private KategoriKoleksi kategori;
    private double skorRataRata;  // 1.0 = Baik, 2.0 = Cukup, 3.0 = Rusak Ringan, 4.0 = Rusak Berat
    private int jumlahPemeriksaan;

    public TrenKondisiItem() {}

    public TrenKondisiItem(String periode, String labelPeriode, KategoriKoleksi kategori,
                           double skorRataRata, int jumlahPemeriksaan) {
        this.periode = periode;
        this.labelPeriode = labelPeriode;
        this.kategori = kategori;
        this.skorRataRata = skorRataRata;
        this.jumlahPemeriksaan = jumlahPemeriksaan;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getLabelPeriode() {
        return labelPeriode;
    }

    public void setLabelPeriode(String labelPeriode) {
        this.labelPeriode = labelPeriode;
    }

    public KategoriKoleksi getKategori() {
        return kategori;
    }

    public void setKategori(KategoriKoleksi kategori) {
        this.kategori = kategori;
    }

    public double getSkorRataRata() {
        return skorRataRata;
    }

    public void setSkorRataRata(double skorRataRata) {
        this.skorRataRata = skorRataRata;
    }

    public int getJumlahPemeriksaan() {
        return jumlahPemeriksaan;
    }

    public void setJumlahPemeriksaan(int jumlahPemeriksaan) {
        this.jumlahPemeriksaan = jumlahPemeriksaan;
    }
}
