package com.museummate.model;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) untuk baris tabel Daftar Prioritas Perawatan Otomatis.
 * Menggabungkan objek Koleksi dengan data kalkulasi skor terkini.
 */
public class PrioritasItem {
    private final Koleksi koleksi;
    private final LocalDate tanggalAcuan;
    private final long hariTerlewat;
    private final double skor;
    private final boolean belumPernahDiperiksa;

    public PrioritasItem(Koleksi koleksi, LocalDate tanggalAcuan, long hariTerlewat, double skor, boolean belumPernahDiperiksa) {
        this.koleksi = koleksi;
        this.tanggalAcuan = tanggalAcuan;
        this.hariTerlewat = hariTerlewat;
        this.skor = skor;
        this.belumPernahDiperiksa = belumPernahDiperiksa;
    }

    public Koleksi getKoleksi() {
        return koleksi;
    }

    public int getIdKoleksi() {
        return koleksi.getIdKoleksi();
    }

    public String getKodeRegistrasi() {
        return koleksi.getKodeRegistrasi();
    }

    public String getNama() {
        return koleksi.getNama();
    }

    public KategoriKoleksi getKategori() {
        return koleksi.getKategori();
    }

    public KondisiKoleksi getKondisi() {
        return koleksi.getKondisi();
    }

    public LocalDate getTanggalAcuan() {
        return tanggalAcuan;
    }

    public long getHariTerlewat() {
        return hariTerlewat;
    }

    public double getSkor() {
        return skor;
    }

    public boolean isBelumPernahDiperiksa() {
        return belumPernahDiperiksa;
    }

    /**
     * Kategori urgensi berdasarkan skor prioritas:
     * - Kritis: skor >= 35
     * - Tinggi: skor >= 25
     * - Sedang: skor >= 15
     * - Rendah: skor < 15
     */
    public String getTingkatUrgensi() {
        if (skor >= 35.0) return "Kritis";
        if (skor >= 25.0) return "Tinggi";
        if (skor >= 15.0) return "Sedang";
        return "Rendah";
    }

    public String getSkorFormatted() {
        return String.format("%.1f", skor);
    }
}
