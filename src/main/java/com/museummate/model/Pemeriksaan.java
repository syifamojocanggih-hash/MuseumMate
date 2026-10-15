package com.museummate.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entitas riwayat pemeriksaan fisik dan konservasi berkala koleksi.
 */
public class Pemeriksaan {
    private int idPemeriksaan;
    private int idKoleksi;
    private int idPengguna;
    private String namaPetugas;
    private String kodeKoleksi;
    private String namaKoleksi;
    private LocalDate tanggalPemeriksaan;
    private KondisiKoleksi kondisiHasil;
    private String tindakan;
    private String catatan;
    private LocalDateTime dibuatPada;

    public Pemeriksaan() {
        this.tanggalPemeriksaan = LocalDate.now();
        this.kondisiHasil = KondisiKoleksi.BAIK;
    }

    public Pemeriksaan(int idKoleksi, int idPengguna, LocalDate tanggalPemeriksaan,
                       KondisiKoleksi kondisiHasil, String tindakan, String catatan) {
        this.idKoleksi = idKoleksi;
        this.idPengguna = idPengguna;
        setTanggalPemeriksaan(tanggalPemeriksaan);
        this.kondisiHasil = (kondisiHasil != null) ? kondisiHasil : KondisiKoleksi.BAIK;
        this.tindakan = tindakan;
        this.catatan = catatan;
    }

    public int getIdPemeriksaan() {
        return idPemeriksaan;
    }

    public void setIdPemeriksaan(int idPemeriksaan) {
        this.idPemeriksaan = idPemeriksaan;
    }

    public int getIdKoleksi() {
        return idKoleksi;
    }

    public void setIdKoleksi(int idKoleksi) {
        this.idKoleksi = idKoleksi;
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(int idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getNamaPetugas() {
        return namaPetugas != null ? namaPetugas : "";
    }

    public void setNamaPetugas(String namaPetugas) {
        this.namaPetugas = namaPetugas;
    }

    public String getKodeKoleksi() {
        return kodeKoleksi != null ? kodeKoleksi : "";
    }

    public void setKodeKoleksi(String kodeKoleksi) {
        this.kodeKoleksi = kodeKoleksi;
    }

    public String getNamaKoleksi() {
        return namaKoleksi != null ? namaKoleksi : "";
    }

    public void setNamaKoleksi(String namaKoleksi) {
        this.namaKoleksi = namaKoleksi;
    }

    public LocalDate getTanggalPemeriksaan() {
        return tanggalPemeriksaan;
    }

    public void setTanggalPemeriksaan(LocalDate tanggalPemeriksaan) {
        if (tanggalPemeriksaan != null && tanggalPemeriksaan.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal pemeriksaan tidak boleh di masa depan.");
        }
        this.tanggalPemeriksaan = (tanggalPemeriksaan != null) ? tanggalPemeriksaan : LocalDate.now();
    }

    public KondisiKoleksi getKondisiHasil() {
        return kondisiHasil;
    }

    public void setKondisiHasil(KondisiKoleksi kondisiHasil) {
        this.kondisiHasil = (kondisiHasil != null) ? kondisiHasil : KondisiKoleksi.BAIK;
    }

    public String getTindakan() {
        return tindakan != null ? tindakan : "";
    }

    public void setTindakan(String tindakan) {
        this.tindakan = tindakan != null ? tindakan.trim() : "";
    }

    public String getCatatan() {
        return catatan != null ? catatan : "";
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan != null ? catatan.trim() : "";
    }

    public LocalDateTime getDibuatPada() {
        return dibuatPada;
    }

    public void setDibuatPada(LocalDateTime dibuatPada) {
        this.dibuatPada = dibuatPada;
    }
}
