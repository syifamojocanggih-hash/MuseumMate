package com.museummate.util;

import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Pengguna;
import com.museummate.model.PeranPengguna;

/**
 * Pengelola sesi login aktif aplikasi MuseumMate.
 * Menerapkan pola Singleton untuk melacak akun yang sedang beroperasi.
 */
public class SessionManager {
    private static SessionManager instance;
    private Pengguna currentUser;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Pengguna user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Pengguna getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getPeran() == PeranPengguna.ADMIN;
    }

    public boolean isKonservator() {
        return currentUser != null && currentUser.getPeran() == PeranPengguna.KONSERVATOR;
    }

    public String getSpesialisasi() {
        return (currentUser != null && currentUser.getSpesialisasi() != null)
                ? currentUser.getSpesialisasi().toUpperCase().trim()
                : "SEMUA";
    }

    public boolean hasSpesialisasi() {
        return isKonservator() && !"SEMUA".equalsIgnoreCase(getSpesialisasi());
    }

    public KategoriKoleksi getKategoriSpesialisasi() {
        if (!hasSpesialisasi()) return null;
        try {
            return KategoriKoleksi.fromString(getSpesialisasi());
        } catch (Exception e) {
            return null;
        }
    }

    public void setSessionSpesialisasi(String spesialisasi) {
        if (currentUser != null) {
            currentUser.setSpesialisasi(spesialisasi);
        }
    }
}
