package com.museummate.service;

import com.museummate.dao.KoleksiDAO;
import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;

import java.sql.SQLException;
import java.util.List;

/**
 * Layanan manajemen data koleksi museum (PRD F2 & F3).
 */
public class KoleksiService {
    private final KoleksiDAO koleksiDAO;

    public KoleksiService() {
        this.koleksiDAO = new KoleksiDAO();
    }

    public KoleksiService(KoleksiDAO koleksiDAO) {
        this.koleksiDAO = koleksiDAO;
    }

    public List<Koleksi> getAllKoleksi() throws SQLException {
        return koleksiDAO.findAll();
    }

    public Koleksi getKoleksiById(int id) throws SQLException {
        return koleksiDAO.findById(id);
    }

    public List<Koleksi> searchAndFilter(String keyword, KategoriKoleksi kategori, KondisiKoleksi kondisi) throws SQLException {
        return koleksiDAO.searchAndFilter(keyword, kategori, kondisi);
    }

    public void tambahKoleksi(Koleksi k) throws Exception {
        validasiDataInti(k);

        // Periksa keunikan kode registrasi (PRD F2)
        Koleksi existing = koleksiDAO.findByKode(k.getKodeRegistrasi());
        if (existing != null) {
            throw new IllegalArgumentException("Kode registrasi '" + k.getKodeRegistrasi() + "' sudah terdaftar. Kode harus unik.");
        }

        koleksiDAO.save(k);
    }

    public void ubahKoleksi(Koleksi k) throws Exception {
        validasiDataInti(k);

        // Periksa kode registrasi pada data lain
        Koleksi existing = koleksiDAO.findByKode(k.getKodeRegistrasi());
        if (existing != null && existing.getIdKoleksi() != k.getIdKoleksi()) {
            throw new IllegalArgumentException("Kode registrasi '" + k.getKodeRegistrasi() + "' sudah digunakan oleh koleksi lain.");
        }

        koleksiDAO.update(k);
    }

    public int cekRiwayatPemeriksaan(int idKoleksi) throws SQLException {
        return koleksiDAO.countPemeriksaan(idKoleksi);
    }

    public boolean hapusKoleksi(int idKoleksi) throws SQLException {
        return koleksiDAO.delete(idKoleksi);
    }

    private void validasiDataInti(Koleksi k) {
        if (k == null) {
            throw new IllegalArgumentException("Data koleksi tidak boleh kosong.");
        }
        if (k.getKodeRegistrasi() == null || k.getKodeRegistrasi().trim().isEmpty()) {
            throw new IllegalArgumentException("Kode registrasi wajib diisi.");
        }
        if (k.getNama() == null || k.getNama().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama koleksi wajib diisi.");
        }
        if (k.getKategori() == null) {
            throw new IllegalArgumentException("Kategori koleksi wajib dipilih.");
        }
        if (k.getKondisi() == null) {
            throw new IllegalArgumentException("Kondisi koleksi wajib dipilih.");
        }
    }
}
