package com.museummate.model;

import java.time.LocalDateTime;

/**
 * Entitas pengguna sistem MuseumMate (Admin dan Konservator).
 */
public class Pengguna {
    private int idPengguna;
    private String username;
    private String passwordHash;
    private String namaLengkap;
    private PeranPengguna peran;
    private LocalDateTime dibuatPada;

    public Pengguna() {
        this.peran = PeranPengguna.KONSERVATOR;
    }

    public Pengguna(String username, String passwordHash, String namaLengkap, PeranPengguna peran) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.namaLengkap = namaLengkap;
        this.peran = peran != null ? peran : PeranPengguna.KONSERVATOR;
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(int idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : "";
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap != null ? namaLengkap.trim() : "";
    }

    public PeranPengguna getPeran() {
        return peran;
    }

    public void setPeran(PeranPengguna peran) {
        this.peran = peran != null ? peran : PeranPengguna.KONSERVATOR;
    }

    public LocalDateTime getDibuatPada() {
        return dibuatPada;
    }

    public void setDibuatPada(LocalDateTime dibuatPada) {
        this.dibuatPada = dibuatPada;
    }

    public boolean isAdmin() {
        return this.peran == PeranPengguna.ADMIN;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", namaLengkap, peran.getLabel());
    }
}
