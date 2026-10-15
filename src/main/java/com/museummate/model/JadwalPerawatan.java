package com.museummate.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entitas jadwal rencana perawatan koleksi museum.
 */
public class JadwalPerawatan {
    private int idJadwal;
    private int idKoleksi;
    private String kodeKoleksi;
    private String namaKoleksi;
    private LocalDate tanggalRencana;
    private String kegiatan;
    private String keterangan;
    private StatusJadwal status;
    private LocalDateTime dibuatPada;
    private LocalDateTime diperbaruiPada;

    public JadwalPerawatan() {
        this.status = StatusJadwal.DIRENCANAKAN;
        this.tanggalRencana = LocalDate.now().plusDays(7);
    }

    public JadwalPerawatan(int idKoleksi, LocalDate tanggalRencana, String kegiatan, String keterangan) {
        this.idKoleksi = idKoleksi;
        this.tanggalRencana = tanggalRencana;
        this.kegiatan = kegiatan;
        this.keterangan = keterangan;
        this.status = StatusJadwal.DIRENCANAKAN;
    }

    /**
     * Memeriksa apakah jadwal telah melewati tanggal rencana dan belum berstatus SELESAI.
     * Sesuai kriteria PRD F5.
     */
    public boolean isTerlambat() {
        if (status == StatusJadwal.SELESAI || status == StatusJadwal.DIBATALKAN) {
            return false;
        }
        return tanggalRencana != null && tanggalRencana.isBefore(LocalDate.now());
    }

    public int getIdJadwal() {
        return idJadwal;
    }

    public void setIdJadwal(int idJadwal) {
        this.idJadwal = idJadwal;
    }

    public int getIdKoleksi() {
        return idKoleksi;
    }

    public void setIdKoleksi(int idKoleksi) {
        this.idKoleksi = idKoleksi;
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

    public LocalDate getTanggalRencana() {
        return tanggalRencana;
    }

    public void setTanggalRencana(LocalDate tanggalRencana) {
        this.tanggalRencana = (tanggalRencana != null) ? tanggalRencana : LocalDate.now();
    }

    public String getKegiatan() {
        return kegiatan != null ? kegiatan : "";
    }

    public void setKegiatan(String kegiatan) {
        this.kegiatan = kegiatan != null ? kegiatan.trim() : "";
    }

    public String getKeterangan() {
        return keterangan != null ? keterangan : "";
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan != null ? keterangan.trim() : "";
    }

    public StatusJadwal getStatus() {
        return status;
    }

    public void setStatus(StatusJadwal status) {
        this.status = (status != null) ? status : StatusJadwal.DIRENCANAKAN;
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
}
