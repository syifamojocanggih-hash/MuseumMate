package com.museummate.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitas enkripsi dan verifikasi kata sandi menggunakan algoritma jBCrypt.
 * Menjamin pemenuhan kebutuhan non-fungsional keamanan (PRD Bagian 7).
 */
public class PasswordUtil {

    /**
     * Menghasilkan hash BCrypt dengan salt otomatis (work factor 10).
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh kosong.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Memverifikasi kecocokan antara password teks biasa dan hash yang tersimpan.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Jika format hash tidak valid
            return false;
        }
    }
}
