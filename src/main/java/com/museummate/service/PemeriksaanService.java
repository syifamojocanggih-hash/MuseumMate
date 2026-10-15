package com.museummate.service;

import com.museummate.dao.PemeriksaanDAO;
import com.museummate.model.Pemeriksaan;
import com.museummate.util.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Layanan pencatatan dan penelusuran riwayat pemeriksaan koleksi (PRD F4).
 */
public class PemeriksaanService {
    private final PemeriksaanDAO pemeriksaanDAO;

    public PemeriksaanService() {
        this.pemeriksaanDAO = new PemeriksaanDAO();
    }

    public PemeriksaanService(PemeriksaanDAO pemeriksaanDAO) {
        this.pemeriksaanDAO = pemeriksaanDAO;
    }

    public List<Pemeriksaan> getRiwayatByKoleksi(int idKoleksi) throws SQLException {
        return pemeriksaanDAO.findByKoleksiId(idKoleksi);
    }

    public List<Pemeriksaan> getRecentPemeriksaan(int limit) throws SQLException {
        return pemeriksaanDAO.findAllRecent(limit);
    }

    public void catatPemeriksaan(Pemeriksaan p) throws Exception {
        if (p == null) {
            throw new IllegalArgumentException("Data pemeriksaan tidak boleh kosong.");
        }
        if (p.getIdKoleksi() <= 0) {
            throw new IllegalArgumentException("ID Koleksi tidak valid.");
        }
        if (p.getTanggalPemeriksaan() != null && p.getTanggalPemeriksaan().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal pemeriksaan tidak boleh di masa depan (PRD F4).");
        }
        if (p.getKondisiHasil() == null) {
            throw new IllegalArgumentException("Kondisi hasil pemeriksaan wajib ditentukan.");
        }
        if (p.getTindakan() == null || p.getTindakan().trim().isEmpty()) {
            throw new IllegalArgumentException("Tindakan pemeriksaan wajib diisi.");
        }

        // Petugas aktif jika belum diset
        if (p.getIdPengguna() <= 0 && SessionManager.getInstance().isLoggedIn()) {
            p.setIdPengguna(SessionManager.getInstance().getCurrentUser().getIdPengguna());
        }

        pemeriksaanDAO.save(p);
    }
}
