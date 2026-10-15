package com.museummate.service;

import com.museummate.dao.KoleksiDAO;
import com.museummate.dao.PemeriksaanDAO;
import com.museummate.model.Koleksi;
import com.museummate.model.PrioritasItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Layanan kalkulasi skor prioritas perawatan otomatis (PRD F6 & 6.4).
 * Ini merupakan fitur pembeda utama MuseumMate.
 *
 * Rumus:
 * Skor Prioritas = (Bobot Kondisi x 10) + (Hari sejak Pemeriksaan Terakhir / 30) x Faktor Kategori
 */
public class PrioritasService {
    private final KoleksiDAO koleksiDAO;
    private final PemeriksaanDAO pemeriksaanDAO;

    public PrioritasService() {
        this.koleksiDAO = new KoleksiDAO();
        this.pemeriksaanDAO = new PemeriksaanDAO();
    }

    public PrioritasService(KoleksiDAO koleksiDAO, PemeriksaanDAO pemeriksaanDAO) {
        this.koleksiDAO = koleksiDAO;
        this.pemeriksaanDAO = pemeriksaanDAO;
    }

    /**
     * Menghitung nilai skor prioritas untuk satu objek koleksi berdasarkan tanggal acuan tertentu.
     * Dapat digunakan secara mandiri untuk unit test rumus PRD 6.4.
     */
    public double hitungSkor(Koleksi koleksi, LocalDate tanggalPemeriksaanTerakhir, LocalDate tanggalSekarang) {
        if (koleksi == null) return 0.0;
        return koleksi.hitungSkorPrioritas(tanggalPemeriksaanTerakhir, tanggalSekarang);
    }

    /**
     * Memuat dan menghitung ulang skor prioritas untuk seluruh koleksi secara dinamis.
     * Hasil diurutkan dari skor tertinggi (paling mendesak) ke terendah.
     *
     * @param tanggalAcuan Tanggal referensi kalkulasi (default: LocalDate.now())
     * @return List PrioritasItem yang telah terurut
     */
    public List<PrioritasItem> getDaftarPrioritas(LocalDate tanggalAcuan) throws SQLException {
        if (tanggalAcuan == null) {
            tanggalAcuan = LocalDate.now();
        }

        List<Koleksi> semuaKoleksi = koleksiDAO.findAll();
        Map<Integer, LocalDate> mapTglPemeriksaan = pemeriksaanDAO.findAllLastInspectionDates();

        List<PrioritasItem> hasil = new ArrayList<>(semuaKoleksi.size());

        for (Koleksi k : semuaKoleksi) {
            LocalDate tglTerakhir = mapTglPemeriksaan.get(k.getIdKoleksi());
            boolean belumPernah = (tglTerakhir == null);
            LocalDate acuanHitung = belumPernah ? k.getTanggalDiterima() : tglTerakhir;

            long hariTerlewat = 0;
            if (acuanHitung != null) {
                hariTerlewat = ChronoUnit.DAYS.between(acuanHitung, tanggalAcuan);
                if (hariTerlewat < 0) hariTerlewat = 0;
            }

            double skor = k.hitungSkorPrioritas(tglTerakhir, tanggalAcuan);

            hasil.add(new PrioritasItem(k, acuanHitung, hariTerlewat, skor, belumPernah));
        }

        // Urutkan dari skor tertinggi ke terendah
        hasil.sort(Comparator.comparingDouble(PrioritasItem::getSkor).reversed());

        return hasil;
    }
}
