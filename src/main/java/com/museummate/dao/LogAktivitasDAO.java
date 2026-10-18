package com.museummate.dao;

import com.museummate.model.LogAktivitas;
import com.museummate.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel log_aktivitas (PRD F10 / UC-13).
 */
public class LogAktivitasDAO {

    private static final String BASE_SELECT =
            "SELECT l.*, u.nama_lengkap AS nama_pengguna, u.username AS username_pengguna " +
            "FROM log_aktivitas l " +
            "LEFT JOIN pengguna u ON l.id_pengguna = u.id_pengguna ";

    public void catat(Integer idPengguna, String jenisAksi, String tabelTerkait, String keterangan) throws SQLException {
        String sql = "INSERT INTO log_aktivitas (id_pengguna, jenis_aksi, tabel_terkait, keterangan) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idPengguna != null && idPengguna > 0) {
                ps.setInt(1, idPengguna);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, jenisAksi != null ? jenisAksi.toUpperCase() : "INFO");
            ps.setString(3, tabelTerkait != null ? tabelTerkait.toLowerCase() : "umum");
            ps.setString(4, keterangan);
            ps.executeUpdate();
        }
    }

    public List<LogAktivitas> findAll(int limit) throws SQLException {
        List<LogAktivitas> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY l.waktu DESC, l.id_log DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 200);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        }
        return list;
    }

    public List<LogAktivitas> findFiltered(String keyword, String jenisAksi, String tabelTerkait) throws SQLException {
        List<LogAktivitas> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (l.keterangan LIKE ? OR u.nama_lengkap LIKE ? OR u.username LIKE ?) ");
            String kwPattern = "%" + keyword.trim() + "%";
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
        }

        if (jenisAksi != null && !jenisAksi.trim().isEmpty() && !jenisAksi.equalsIgnoreCase("SEMUA")) {
            sql.append("AND l.jenis_aksi = ? ");
            params.add(jenisAksi.toUpperCase().trim());
        }

        if (tabelTerkait != null && !tabelTerkait.trim().isEmpty() && !tabelTerkait.equalsIgnoreCase("SEMUA")) {
            sql.append("AND l.tabel_terkait = ? ");
            params.add(tabelTerkait.toLowerCase().trim());
        }

        sql.append("ORDER BY l.waktu DESC, l.id_log DESC LIMIT 200");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        }
        return list;
    }

    private LogAktivitas mapResultSetToLog(ResultSet rs) throws SQLException {
        LogAktivitas log = new LogAktivitas();
        log.setIdLog(rs.getInt("id_log"));
        int idPengguna = rs.getInt("id_pengguna");
        if (!rs.wasNull()) {
            log.setIdPengguna(idPengguna);
        }
        log.setNamaPengguna(rs.getString("nama_pengguna"));
        log.setUsernamePengguna(rs.getString("username_pengguna"));
        log.setJenisAksi(rs.getString("jenis_aksi"));
        log.setTabelTerkait(rs.getString("tabel_terkait"));
        log.setKeterangan(rs.getString("keterangan"));

        Timestamp ts = rs.getTimestamp("waktu");
        if (ts != null) {
            log.setWaktu(ts.toLocalDateTime());
        }
        return log;
    }
}
