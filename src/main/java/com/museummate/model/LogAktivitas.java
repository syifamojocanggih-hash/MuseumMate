package com.museummate.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model domain untuk merepresentasikan riwayat audit log aktivitas sistem (PRD F10 / UC-13).
 * Menerapkan prinsip Enkapsulasi murni dengan field private dan validasi.
 */
public class LogAktivitas {
    private int idLog;
    private Integer idPengguna;
    private String namaPengguna;
    private String usernamePengguna;
    private String jenisAksi;     // TAMBAH, UBAH, HAPUS, LOGIN
    private String tabelTerkait;  // koleksi, pengguna, jadwal_perawatan, pemeriksaan, dll.
    private String keterangan;
    private LocalDateTime waktu;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public LogAktivitas() {
        this.waktu = LocalDateTime.now();
    }

    public LogAktivitas(Integer idPengguna, String jenisAksi, String tabelTerkait, String keterangan) {
        this();
        this.idPengguna = idPengguna;
        this.jenisAksi = jenisAksi;
        this.tabelTerkait = tabelTerkait;
        this.keterangan = keterangan;
    }

    public LogAktivitas(int idLog, Integer idPengguna, String namaPengguna, String usernamePengguna,
                        String jenisAksi, String tabelTerkait, String keterangan, LocalDateTime waktu) {
        this.idLog = idLog;
        this.idPengguna = idPengguna;
        this.namaPengguna = namaPengguna;
        this.usernamePengguna = usernamePengguna;
        this.jenisAksi = jenisAksi;
        this.tabelTerkait = tabelTerkait;
        this.keterangan = keterangan;
        this.waktu = waktu;
    }

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public Integer getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(Integer idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getNamaPengguna() {
        return namaPengguna != null ? namaPengguna : (usernamePengguna != null ? usernamePengguna : "Sistem");
    }

    public void setNamaPengguna(String namaPengguna) {
        this.namaPengguna = namaPengguna;
    }

    public String getUsernamePengguna() {
        return usernamePengguna;
    }

    public void setUsernamePengguna(String usernamePengguna) {
        this.usernamePengguna = usernamePengguna;
    }

    public String getJenisAksi() {
        return jenisAksi;
    }

    public void setJenisAksi(String jenisAksi) {
        this.jenisAksi = jenisAksi;
    }

    public String getTabelTerkait() {
        return tabelTerkait;
    }

    public void setTabelTerkait(String tabelTerkait) {
        this.tabelTerkait = tabelTerkait;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public void setWaktu(LocalDateTime waktu) {
        this.waktu = waktu;
    }

    public String getWaktuFormatted() {
        if (waktu == null) return "-";
        return waktu.format(DISPLAY_FORMATTER);
    }

    @Override
    public String toString() {
        return "[" + getWaktuFormatted() + "] " + getNamaPengguna() + " - " + jenisAksi + " on " + tabelTerkait + ": " + keterangan;
    }
}
