package com.museummate.service;

import com.museummate.dao.JadwalPerawatanDAO;
import com.museummate.dao.KoleksiDAO;
import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.PrioritasItem;
import com.museummate.util.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Layanan kalkulasi ringkasan statistik operasional untuk Dashboard (PRD F7).
 */
public class DashboardService {
    private final KoleksiDAO koleksiDAO;
    private final JadwalPerawatanDAO jadwalDAO;
    private final com.museummate.dao.PemeriksaanDAO pemeriksaanDAO;
    private final PrioritasService prioritasService;

    public DashboardService() {
        this.koleksiDAO = new KoleksiDAO();
        this.jadwalDAO = new JadwalPerawatanDAO();
        this.pemeriksaanDAO = new com.museummate.dao.PemeriksaanDAO();
        this.prioritasService = new PrioritasService();
    }

    public DashboardService(KoleksiDAO koleksiDAO, JadwalPerawatanDAO jadwalDAO, PrioritasService prioritasService) {
        this.koleksiDAO = koleksiDAO;
        this.jadwalDAO = jadwalDAO;
        this.pemeriksaanDAO = new com.museummate.dao.PemeriksaanDAO();
        this.prioritasService = prioritasService;
    }

    public DashboardStats getStats() throws SQLException {
        KategoriKoleksi filter = null;
        try {
            filter = SessionManager.getInstance().getKategoriSpesialisasi();
        } catch (Exception ignored) {}
        return getStats(filter);
    }

    public DashboardStats getStats(KategoriKoleksi filterKategori) throws SQLException {
        List<Koleksi> all = koleksiDAO.findAll();
        if (filterKategori != null) {
            all = all.stream().filter(k -> k.getKategori() == filterKategori).toList();
        }
        int totalKoleksi = all.size();

        Map<KategoriKoleksi, Integer> perKategori = new EnumMap<>(KategoriKoleksi.class);
        for (KategoriKoleksi k : KategoriKoleksi.values()) {
            perKategori.put(k, 0);
        }

        Map<KondisiKoleksi, Integer> perKondisi = new EnumMap<>(KondisiKoleksi.class);
        for (KondisiKoleksi k : KondisiKoleksi.values()) {
            perKondisi.put(k, 0);
        }

        for (Koleksi k : all) {
            perKategori.put(k.getKategori(), perKategori.get(k.getKategori()) + 1);
            perKondisi.put(k.getKondisi(), perKondisi.get(k.getKondisi()) + 1);
        }

        List<com.museummate.model.JadwalPerawatan> allJadwal = jadwalDAO.findAll();
        if (filterKategori != null) {
            allJadwal = allJadwal.stream().filter(j -> j.getKategoriKoleksi() == filterKategori).toList();
        }

        int jumlahTerlambat = 0;
        int jumlahMendatang = 0;
        LocalDate now = LocalDate.now();
        LocalDate nextWeek = now.plusDays(7);
        for (com.museummate.model.JadwalPerawatan j : allJadwal) {
            if (j.isTerlambat()) {
                jumlahTerlambat++;
            }
            if (j.getStatus() == com.museummate.model.StatusJadwal.DIRENCANAKAN && j.getTanggalRencana() != null) {
                if (!j.getTanggalRencana().isBefore(now) && !j.getTanggalRencana().isAfter(nextWeek)) {
                    jumlahMendatang++;
                }
            }
        }

        List<PrioritasItem> listPrioritas = prioritasService.getDaftarPrioritas(LocalDate.now());
        if (filterKategori != null) {
            listPrioritas = listPrioritas.stream().filter(pi -> pi.getKategori() == filterKategori).toList();
        }

        int jumlahKritis = 0;
        for (PrioritasItem pi : listPrioritas) {
            if ("Kritis".equalsIgnoreCase(pi.getTingkatUrgensi()) || "Tinggi".equalsIgnoreCase(pi.getTingkatUrgensi())) {
                jumlahKritis++;
            }
        }

        List<com.museummate.model.TrenKondisiItem> trenKondisi = pemeriksaanDAO.getTrenKondisi();
        if (filterKategori != null) {
            trenKondisi = trenKondisi.stream().filter(t -> t.getKategori() == filterKategori).toList();
        }

        List<com.museummate.model.JadwalPerawatan> upcomingJadwal = allJadwal.stream()
                .filter(j -> j.getStatus() == com.museummate.model.StatusJadwal.DIRENCANAKAN)
                .sorted((a, b) -> a.getTanggalRencana().compareTo(b.getTanggalRencana()))
                .limit(5)
                .toList();

        return new DashboardStats(totalKoleksi, perKategori, perKondisi, jumlahTerlambat, jumlahMendatang, jumlahKritis, listPrioritas, trenKondisi, upcomingJadwal);
    }

    public record DashboardStats(
            int totalKoleksi,
            Map<KategoriKoleksi, Integer> perKategori,
            Map<KondisiKoleksi, Integer> perKondisi,
            int jumlahJadwalTerlambat,
            int jumlahJadwalMendatang,
            int jumlahPrioritasMendesak,
            List<PrioritasItem> topPrioritas,
            List<com.museummate.model.TrenKondisiItem> trenKondisi,
            List<com.museummate.model.JadwalPerawatan> upcomingJadwal
    ) {}
}

