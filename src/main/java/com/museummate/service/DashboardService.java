package com.museummate.service;

import com.museummate.dao.JadwalPerawatanDAO;
import com.museummate.dao.KoleksiDAO;
import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.PrioritasItem;

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
    private final PrioritasService prioritasService;

    public DashboardService() {
        this.koleksiDAO = new KoleksiDAO();
        this.jadwalDAO = new JadwalPerawatanDAO();
        this.prioritasService = new PrioritasService();
    }

    public DashboardService(KoleksiDAO koleksiDAO, JadwalPerawatanDAO jadwalDAO, PrioritasService prioritasService) {
        this.koleksiDAO = koleksiDAO;
        this.jadwalDAO = jadwalDAO;
        this.prioritasService = prioritasService;
    }

    public DashboardStats getStats() throws SQLException {
        List<Koleksi> all = koleksiDAO.findAll();
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

        int jumlahTerlambat = jadwalDAO.countOverdue();

        List<PrioritasItem> listPrioritas = prioritasService.getDaftarPrioritas(LocalDate.now());
        int jumlahKritis = 0;
        for (PrioritasItem pi : listPrioritas) {
            if ("Kritis".equalsIgnoreCase(pi.getTingkatUrgensi()) || "Tinggi".equalsIgnoreCase(pi.getTingkatUrgensi())) {
                jumlahKritis++;
            }
        }

        return new DashboardStats(totalKoleksi, perKategori, perKondisi, jumlahTerlambat, jumlahKritis, listPrioritas);
    }

    public record DashboardStats(
            int totalKoleksi,
            Map<KategoriKoleksi, Integer> perKategori,
            Map<KondisiKoleksi, Integer> perKondisi,
            int jumlahJadwalTerlambat,
            int jumlahPrioritasMendesak,
            List<PrioritasItem> topPrioritas
    ) {}
}
