package com.museummate.service;

import com.museummate.dao.JadwalPerawatanDAO;
import com.museummate.model.JadwalPerawatan;
import com.museummate.model.StatusJadwal;

import java.sql.SQLException;
import java.util.List;

/**
 * Layanan penjadwalan dan pemantauan perawatan museum (PRD F5).
 */
public class JadwalService {
    private final JadwalPerawatanDAO jadwalDAO;

    public JadwalService() {
        this.jadwalDAO = new JadwalPerawatanDAO();
    }

    public JadwalService(JadwalPerawatanDAO jadwalDAO) {
        this.jadwalDAO = jadwalDAO;
    }

    public List<JadwalPerawatan> getAllJadwal() throws SQLException {
        return jadwalDAO.findAll();
    }

    public List<JadwalPerawatan> getUpcomingJadwal(int limit) throws SQLException {
        return jadwalDAO.findUpcoming(limit);
    }

    public int getJumlahTerlambat() throws SQLException {
        return jadwalDAO.countOverdue();
    }

    public List<JadwalPerawatan> getJadwalTerlambat() throws SQLException {
        return jadwalDAO.findOverdue();
    }

    public List<JadwalPerawatan> getJadwalMendatang(int days) throws SQLException {
        return jadwalDAO.findDueSoon(days);
    }

    public int getJumlahMendatang(int days) throws SQLException {
        return jadwalDAO.countDueSoon(days);
    }

    public int getJumlahNotifikasiAktif() throws SQLException {
        return jadwalDAO.countOverdue() + jadwalDAO.countDueSoon(7);
    }


    public void tambahJadwal(JadwalPerawatan j) throws Exception {
        if (j == null) {
            throw new IllegalArgumentException("Data jadwal tidak boleh kosong.");
        }
        if (j.getIdKoleksi() <= 0) {
            throw new IllegalArgumentException("Koleksi target perawatan wajib dipilih.");
        }
        if (j.getTanggalRencana() == null) {
            throw new IllegalArgumentException("Tanggal rencana perawatan wajib ditentukan.");
        }
        if (j.getKegiatan() == null || j.getKegiatan().trim().isEmpty()) {
            throw new IllegalArgumentException("Kegiatan perawatan wajib diisi.");
        }

        jadwalDAO.save(j);
    }

    public boolean ubahStatus(int idJadwal, StatusJadwal status) throws SQLException {
        return jadwalDAO.updateStatus(idJadwal, status);
    }

    public boolean hapusJadwal(int idJadwal) throws SQLException {
        return jadwalDAO.delete(idJadwal);
    }
}
