package com.museummate.service;

import com.museummate.dao.LogAktivitasDAO;
import com.museummate.model.LogAktivitas;
import com.museummate.model.Pengguna;
import com.museummate.util.SessionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Service untuk pencatatan dan pengelolaan log audit aktivitas sistem (PRD F10 / UC-13).
 */
public class LogAktivitasService {

    private static final LogAktivitasDAO dao = new LogAktivitasDAO();

    /**
     * Helper static untuk mencatat log aktivitas pengguna yang sedang login.
     * Didesain fail-safe agar kegagalan log tidak mengganggu jalannya transaksi utama pengguna.
     */
    public static void catat(String jenisAksi, String tabelTerkait, String keterangan) {
        try {
            Pengguna currentUser = SessionManager.getInstance().getCurrentUser();
            Integer idPengguna = currentUser != null ? currentUser.getIdPengguna() : null;
            dao.catat(idPengguna, jenisAksi, tabelTerkait, keterangan);
        } catch (Exception e) {
            System.err.println("[AuditLog] Gagal mencatat log (" + jenisAksi + " - " + tabelTerkait + "): " + e.getMessage());
        }
    }

    public List<LogAktivitas> getAllLogs() throws SQLException {
        return dao.findAll(200);
    }

    public List<LogAktivitas> filterLogs(String keyword, String jenisAksi, String tabelTerkait) throws SQLException {
        return dao.findFiltered(keyword, jenisAksi, tabelTerkait);
    }
}
