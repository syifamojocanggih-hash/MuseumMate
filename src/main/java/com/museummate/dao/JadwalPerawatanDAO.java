package com.museummate.dao;

import com.museummate.model.JadwalPerawatan;
import com.museummate.model.StatusJadwal;
import com.museummate.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk pengelolaan jadwal perawatan koleksi (PRD F5).
 */
public class JadwalPerawatanDAO {

    private static final String BASE_SELECT_SQL =
            "SELECT j.*, k.kode_registrasi, k.nama AS nama_koleksi " +
            "FROM jadwal_perawatan j " +
            "JOIN koleksi k ON j.id_koleksi = k.id_koleksi ";

    public List<JadwalPerawatan> findAll() throws SQLException {
        List<JadwalPerawatan> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY j.status ASC, j.tanggal_rencana ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToJadwal(rs));
            }
        }
        return list;
    }

    public List<JadwalPerawatan> findByKoleksiId(int idKoleksi) throws SQLException {
        List<JadwalPerawatan> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE j.id_koleksi = ? ORDER BY j.tanggal_rencana DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKoleksi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToJadwal(rs));
                }
            }
        }
        return list;
    }

    public List<JadwalPerawatan> findUpcoming(int limit) throws SQLException {
        List<JadwalPerawatan> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE j.status = 'DIRENCANAKAN' ORDER BY j.tanggal_rencana ASC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToJadwal(rs));
                }
            }
        }
        return list;
    }

    public int countOverdue() throws SQLException {
        String sql = "SELECT COUNT(*) FROM jadwal_perawatan WHERE status = 'DIRENCANAKAN' AND tanggal_rencana < CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<JadwalPerawatan> findOverdue() throws SQLException {
        List<JadwalPerawatan> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE j.status = 'DIRENCANAKAN' AND j.tanggal_rencana < CURDATE() ORDER BY j.tanggal_rencana ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToJadwal(rs));
            }
        }
        return list;
    }

    public List<JadwalPerawatan> findDueSoon(int days) throws SQLException {
        List<JadwalPerawatan> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE j.status = 'DIRENCANAKAN' AND j.tanggal_rencana >= CURDATE() " +
                     "AND j.tanggal_rencana <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY j.tanggal_rencana ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToJadwal(rs));
                }
            }
        }
        return list;
    }

    public int countDueSoon(int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM jadwal_perawatan WHERE status = 'DIRENCANAKAN' AND tanggal_rencana >= CURDATE() " +
                     "AND tanggal_rencana <= DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }


    public boolean save(JadwalPerawatan j) throws SQLException {
        String sql = "INSERT INTO jadwal_perawatan (id_koleksi, tanggal_rencana, kegiatan, keterangan, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, j.getIdKoleksi());
            ps.setDate(2, Date.valueOf(j.getTanggalRencana()));
            ps.setString(3, j.getKegiatan());
            ps.setString(4, j.getKeterangan());
            ps.setString(5, j.getStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        j.setIdJadwal(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateStatus(int idJadwal, StatusJadwal newStatus) throws SQLException {
        String sql = "UPDATE jadwal_perawatan SET status = ? WHERE id_jadwal = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, idJadwal);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idJadwal) throws SQLException {
        String sql = "DELETE FROM jadwal_perawatan WHERE id_jadwal = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJadwal);
            return ps.executeUpdate() > 0;
        }
    }

    private JadwalPerawatan mapResultSetToJadwal(ResultSet rs) throws SQLException {
        JadwalPerawatan j = new JadwalPerawatan();
        j.setIdJadwal(rs.getInt("id_jadwal"));
        j.setIdKoleksi(rs.getInt("id_koleksi"));
        j.setKodeKoleksi(rs.getString("kode_registrasi"));
        j.setNamaKoleksi(rs.getString("nama_koleksi"));

        Date tgl = rs.getDate("tanggal_rencana");
        if (tgl != null) {
            j.setTanggalRencana(tgl.toLocalDate());
        }

        j.setKegiatan(rs.getString("kegiatan"));
        j.setKeterangan(rs.getString("keterangan"));
        j.setStatus(StatusJadwal.fromString(rs.getString("status")));

        Timestamp dibuat = rs.getTimestamp("dibuat_pada");
        if (dibuat != null) {
            j.setDibuatPada(dibuat.toLocalDateTime());
        }

        Timestamp diperbarui = rs.getTimestamp("diperbarui_pada");
        if (diperbarui != null) {
            j.setDiperbaruiPada(diperbarui.toLocalDateTime());
        }

        return j;
    }
}
