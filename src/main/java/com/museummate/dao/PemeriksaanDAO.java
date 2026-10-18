package com.museummate.dao;

import com.museummate.model.KondisiKoleksi;
import com.museummate.model.Pemeriksaan;
import com.museummate.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object untuk pencatatan dan riwayat pemeriksaan fisik koleksi.
 */
public class PemeriksaanDAO {

    public List<Pemeriksaan> findByKoleksiId(int idKoleksi) throws SQLException {
        List<Pemeriksaan> list = new ArrayList<>();
        String sql = "SELECT p.*, u.nama_lengkap AS nama_petugas, k.kode_registrasi, k.nama AS nama_koleksi " +
                     "FROM pemeriksaan p " +
                     "JOIN pengguna u ON p.id_pengguna = u.id_pengguna " +
                     "JOIN koleksi k ON p.id_koleksi = k.id_koleksi " +
                     "WHERE p.id_koleksi = ? " +
                     "ORDER BY p.tanggal_pemeriksaan DESC, p.id_pemeriksaan DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKoleksi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPemeriksaan(rs));
                }
            }
        }
        return list;
    }

    public List<Pemeriksaan> findAllRecent(int limit) throws SQLException {
        List<Pemeriksaan> list = new ArrayList<>();
        String sql = "SELECT p.*, u.nama_lengkap AS nama_petugas, k.kode_registrasi, k.nama AS nama_koleksi " +
                     "FROM pemeriksaan p " +
                     "JOIN pengguna u ON p.id_pengguna = u.id_pengguna " +
                     "JOIN koleksi k ON p.id_koleksi = k.id_koleksi " +
                     "ORDER BY p.tanggal_pemeriksaan DESC, p.id_pemeriksaan DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPemeriksaan(rs));
                }
            }
        }
        return list;
    }

    public LocalDate findLastPemeriksaanDate(int idKoleksi) throws SQLException {
        String sql = "SELECT MAX(tanggal_pemeriksaan) FROM pemeriksaan WHERE id_koleksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKoleksi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate(1);
                    if (d != null) {
                        return d.toLocalDate();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Memetakan tanggal pemeriksaan terakhir untuk seluruh koleksi secara efisien (Batch/Map)
     * untuk mempercepat kalkulasi prioritas ≤ 1.000 data (PRD Bagian 7).
     */
    public Map<Integer, LocalDate> findAllLastInspectionDates() throws SQLException {
        Map<Integer, LocalDate> map = new HashMap<>();
        String sql = "SELECT id_koleksi, MAX(tanggal_pemeriksaan) AS tgl_terakhir FROM pemeriksaan GROUP BY id_koleksi";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date d = rs.getDate("tgl_terakhir");
                if (d != null) {
                    map.put(rs.getInt("id_koleksi"), d.toLocalDate());
                }
            }
        }
        return map;
    }

    /**
     * Mengambil data agregasi tren perubahan kondisi koleksi dari waktu ke waktu per kategori (PRD F9 / UC-12).
     */
    public List<com.museummate.model.TrenKondisiItem> getTrenKondisi() throws SQLException {
        List<com.museummate.model.TrenKondisiItem> list = new ArrayList<>();
        String sql = "SELECT " +
                     "    k.kategori, " +
                     "    DATE_FORMAT(p.tanggal_pemeriksaan, '%Y-%m') AS periode, " +
                     "    DATE_FORMAT(p.tanggal_pemeriksaan, '%b %Y') AS label_periode, " +
                     "    AVG(CASE p.kondisi_hasil " +
                     "        WHEN 'BAIK' THEN 1.0 " +
                     "        WHEN 'CUKUP' THEN 2.0 " +
                     "        WHEN 'RUSAK_RINGAN' THEN 3.0 " +
                     "        WHEN 'RUSAK_BERAT' THEN 4.0 " +
                     "        ELSE 1.0 END) AS skor_rata_kondisi, " +
                     "    COUNT(*) AS jumlah_pemeriksaan " +
                     "FROM pemeriksaan p " +
                     "JOIN koleksi k ON p.id_koleksi = k.id_koleksi " +
                     "GROUP BY k.kategori, periode, label_periode " +
                     "ORDER BY periode ASC, k.kategori ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                com.museummate.model.TrenKondisiItem item = new com.museummate.model.TrenKondisiItem(
                        rs.getString("periode"),
                        rs.getString("label_periode"),
                        com.museummate.model.KategoriKoleksi.fromString(rs.getString("kategori")),
                        rs.getDouble("skor_rata_kondisi"),
                        rs.getInt("jumlah_pemeriksaan")
                );
                list.add(item);
            }
        }
        return list;
    }


    /**
     * Menyimpan data pemeriksaan baru dan secara otomatis memperbarui kolom kondisi pada koleksi terkait (PRD F4).
     */
    public boolean save(Pemeriksaan p) throws SQLException {
        String sqlPemeriksaan = "INSERT INTO pemeriksaan (id_koleksi, id_pengguna, tanggal_pemeriksaan, kondisi_hasil, tindakan, catatan) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUpdateKoleksi = "UPDATE koleksi SET kondisi = ? WHERE id_koleksi = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlPemeriksaan, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, p.getIdKoleksi());
                    ps.setInt(2, p.getIdPengguna());
                    ps.setDate(3, Date.valueOf(p.getTanggalPemeriksaan()));
                    ps.setString(4, p.getKondisiHasil().name());
                    ps.setString(5, p.getTindakan());
                    ps.setString(6, p.getCatatan());

                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            p.setIdPemeriksaan(rs.getInt(1));
                        }
                    }
                }

                try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateKoleksi)) {
                    psUpdate.setString(1, p.getKondisiHasil().name());
                    psUpdate.setInt(2, p.getIdKoleksi());
                    psUpdate.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Pemeriksaan mapResultSetToPemeriksaan(ResultSet rs) throws SQLException {
        Pemeriksaan p = new Pemeriksaan();
        p.setIdPemeriksaan(rs.getInt("id_pemeriksaan"));
        p.setIdKoleksi(rs.getInt("id_koleksi"));
        p.setIdPengguna(rs.getInt("id_pengguna"));
        p.setNamaPetugas(rs.getString("nama_petugas"));
        p.setKodeKoleksi(rs.getString("kode_registrasi"));
        p.setNamaKoleksi(rs.getString("nama_koleksi"));

        Date tgl = rs.getDate("tanggal_pemeriksaan");
        if (tgl != null) {
            p.setTanggalPemeriksaan(tgl.toLocalDate());
        }

        p.setKondisiHasil(KondisiKoleksi.fromString(rs.getString("kondisi_hasil")));
        p.setTindakan(rs.getString("tindakan"));
        p.setCatatan(rs.getString("catatan"));

        Timestamp ts = rs.getTimestamp("dibuat_pada");
        if (ts != null) {
            p.setDibuatPada(ts.toLocalDateTime());
        }

        return p;
    }
}
