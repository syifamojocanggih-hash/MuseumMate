package com.museummate.service;

import com.museummate.dao.PenggunaDAO;
import com.museummate.model.Pengguna;
import com.museummate.util.PasswordUtil;
import com.museummate.util.SessionManager;

import java.sql.SQLException;

/**
 * Layanan autentikasi pengguna dan manajemen sesi login (PRD F1).
 */
public class AuthService {
    private final PenggunaDAO penggunaDAO;

    public AuthService() {
        this.penggunaDAO = new PenggunaDAO();
    }

    public AuthService(PenggunaDAO penggunaDAO) {
        this.penggunaDAO = penggunaDAO;
    }

    /**
     * Memproses login pengguna dengan validasi hash BCrypt.
     */
    public Pengguna login(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username tidak boleh kosong.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh kosong.");
        }

        Pengguna user;
        try {
            user = penggunaDAO.findByUsername(username.trim());
        } catch (SQLException e) {
            throw new Exception("Gagal terhubung ke basis data: " + e.getMessage(), e);
        }

        if (user == null) {
            throw new IllegalArgumentException("Username atau password salah.");
        }

        // Verifikasi kata sandi dengan jBCrypt
        boolean matched = PasswordUtil.checkPassword(password, user.getPasswordHash());
        if (!matched) {
            throw new IllegalArgumentException("Username atau password salah.");
        }

        // Simpan sesi aktif
        SessionManager.getInstance().login(user);
        return user;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}
