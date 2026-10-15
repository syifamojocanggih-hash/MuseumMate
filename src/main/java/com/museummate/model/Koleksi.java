package com.museummate.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Representasi kelas abstrak Koleksi yang menjadi dasar bagi semua jenis benda museum.
 * Menerapkan prinsip Abstraksi, Enkapsulasi, dan Polimorfisme (OOP).
 */
public abstract class Koleksi {
    private int idKoleksi;
    private String kodeRegistrasi;
    private String nama;
    private KategoriKoleksi kategori;
    private String asal;
    private String tahunPerkiraan;
    private String bahan;
    private String lokasiPenyimpanan;
    private String sumberPerolehan;
    private KondisiKoleksi kondisi;
    private LocalDate tanggalDiterima;
    private LocalDateTime dibuatPada;
    private LocalDateTime diperbaruiPada;

    public Koleksi() {
        this.kondisi = KondisiKoleksi.BAIK;
        this.tanggalDiterima = LocalDate.now();
    }

    public Koleksi(String kodeRegistrasi, String nama, KategoriKoleksi kategori,
                   String asal, String tahunPerkiraan, String bahan,
                   String lokasiPenyimpanan, String sumberPerolehan,
                   KondisiKoleksi kondisi, LocalDate tanggalDiterima) {
        setKodeRegistrasi(kodeRegistrasi);
        setNama(nama);
        this.kategori = kategori;
        this.asal = asal;
        this.tahunPerkiraan = tahunPerkiraan;
        this.bahan = bahan;
        this.lokasiPenyimpanan = lokasiPenyimpanan;
        this.sumberPerolehan = sumberPerolehan;
        setKondisi(kondisi);
        setTanggalDiterima(tanggalDiterima);
    }

    // =========================================================================
    // Abstract Methods (Prinsip Abstraksi & Polimorfisme)
    // =========================================================================

    /**
     * Mengembalikan faktor risiko kerentanan kategori sesuai PRD 6.4.
     */
    public abstract double getFaktorKategori();

    /**
     * Rekomendasi interval standar pemeriksaan berkala (dalam hari).
     */
    public abstract int getIntervalPemeriksaanHari();

    /**
     * Ringkasan atribut spesifik kategori untuk keperluan tampilan antarmuka.
     */
    public abstract String getDeskripsiKhusus();

    // =========================================================================
    // Core Business Logic (Perhitungan Skor Prioritas PRD 6.4)
    // =========================================================================

    /**
     * Menghitung skor prioritas perawatan otomatis:
     * Skor Prioritas = (Bobot Kondisi x 10) + (Hari sejak Pemeriksaan Terakhir / 30) x Faktor Kategori
     *
     * Jika belum pernah diperiksa, acuan dihitung dari tanggal diterima.
     *
     * @param tanggalPemeriksaanTerakhir Tanggal pemeriksaan terakhir, atau null jika belum pernah
     * @param tanggalSekarang Tanggal acuan saat ini (biasanya LocalDate.now())
     * @return Nilai skor prioritas (semakin tinggi semakin mendesak)
     */
    public double hitungSkorPrioritas(LocalDate tanggalPemeriksaanTerakhir, LocalDate tanggalSekarang) {
        if (tanggalSekarang == null) {
            tanggalSekarang = LocalDate.now();
        }

        LocalDate acuan = (tanggalPemeriksaanTerakhir != null) ? tanggalPemeriksaanTerakhir : this.tanggalDiterima;
        if (acuan == null) {
            acuan = tanggalSekarang;
        }

        long hariTerlewat = ChronoUnit.DAYS.between(acuan, tanggalSekarang);
        if (hariTerlewat < 0) {
            hariTerlewat = 0;
        }

        int bobotKondisi = (this.kondisi != null) ? this.kondisi.getBobot() : 1;
        double faktorKategori = getFaktorKategori();

        // Rumus PRD: (Bobot Kondisi x 10) + (Hari / 30.0) x Faktor Kategori
        return (bobotKondisi * 10.0) + ((hariTerlewat / 30.0) * faktorKategori);
    }

    // =========================================================================
    // Getters and Setters dengan Validasi Enkapsulasi
    // =========================================================================

    public int getIdKoleksi() {
        return idKoleksi;
    }

    public void setIdKoleksi(int idKoleksi) {
        this.idKoleksi = idKoleksi;
    }

    public String getKodeRegistrasi() {
        return kodeRegistrasi;
    }

    public void setKodeRegistrasi(String kodeRegistrasi) {
        if (kodeRegistrasi == null || kodeRegistrasi.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode registrasi koleksi tidak boleh kosong.");
        }
        this.kodeRegistrasi = kodeRegistrasi.trim();
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama koleksi tidak boleh kosong.");
        }
        this.nama = nama.trim();
    }

    public KategoriKoleksi getKategori() {
        return kategori;
    }

    public void setKategori(KategoriKoleksi kategori) {
        this.kategori = (kategori != null) ? kategori : KategoriKoleksi.ARTEFAK;
    }

    public String getAsal() {
        return asal != null ? asal : "";
    }

    public void setAsal(String asal) {
        this.asal = asal != null ? asal.trim() : "";
    }

    public String getTahunPerkiraan() {
        return tahunPerkiraan != null ? tahunPerkiraan : "";
    }

    public void setTahunPerkiraan(String tahunPerkiraan) {
        this.tahunPerkiraan = tahunPerkiraan != null ? tahunPerkiraan.trim() : "";
    }

    public String getBahan() {
        return bahan != null ? bahan : "";
    }

    public void setBahan(String bahan) {
        this.bahan = bahan != null ? bahan.trim() : "";
    }

    public String getLokasiPenyimpanan() {
        return lokasiPenyimpanan != null ? lokasiPenyimpanan : "";
    }

    public void setLokasiPenyimpanan(String lokasiPenyimpanan) {
        this.lokasiPenyimpanan = lokasiPenyimpanan != null ? lokasiPenyimpanan.trim() : "";
    }

    public String getSumberPerolehan() {
        return sumberPerolehan != null ? sumberPerolehan : "";
    }

    public void setSumberPerolehan(String sumberPerolehan) {
        this.sumberPerolehan = sumberPerolehan != null ? sumberPerolehan.trim() : "";
    }

    public KondisiKoleksi getKondisi() {
        return kondisi;
    }

    public void setKondisi(KondisiKoleksi kondisi) {
        if (kondisi == null) {
            throw new IllegalArgumentException("Kondisi koleksi tidak boleh kosong.");
        }
        this.kondisi = kondisi;
    }

    public LocalDate getTanggalDiterima() {
        return tanggalDiterima;
    }

    public void setTanggalDiterima(LocalDate tanggalDiterima) {
        if (tanggalDiterima == null) {
            this.tanggalDiterima = LocalDate.now();
        } else {
            this.tanggalDiterima = tanggalDiterima;
        }
    }

    public LocalDateTime getDibuatPada() {
        return dibuatPada;
    }

    public void setDibuatPada(LocalDateTime dibuatPada) {
        this.dibuatPada = dibuatPada;
    }

    public LocalDateTime getDiperbaruiPada() {
        return diperbaruiPada;
    }

    public void setDiperbaruiPada(LocalDateTime diperbaruiPada) {
        this.diperbaruiPada = diperbaruiPada;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", kodeRegistrasi, nama, kondisi.getLabel());
    }
}
