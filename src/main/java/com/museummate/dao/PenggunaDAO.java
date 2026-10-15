package com.museummate.dao;

import com.museummate.model.Pengguna;
import com.museummate.model.PeranPengguna;
import com.museummate.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk operasi tabel pengguna.
 */
public class PenggunaDAO {

    public Pengguna findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM pengguna WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPengguna(rs);
                }
            }
        }
        return null;
    }

    public Pengguna findById(int id) throws SQLException {
        String sql = "SELECT * FROM pengguna WHERE id_pengguna = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPengguna(rs);
                }
            }
        }
        return null;
    }

    public List<Pengguna> findAll() throws SQLException {
        List<Pengguna> list = new ArrayList<>();
        String sql = "SELECT * FROM pengguna ORDER BY nama_lengkap ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPengguna(rs));
            }
        }
        return list;
    }

    public boolean save(Pengguna user) throws SQLException {
        String sql = "INSERT INTO pengguna (username, password_hash, nama_lengkap, peran) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getNamaLengkap());
            ps.setString(4, user.getPeran().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setIdPengguna(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Pengguna user, boolean updatePassword) throws SQLException {
        String sql;
        if (updatePassword) {
            sql = "UPDATE pengguna SET username = ?, password_hash = ?, nama_lengkap = ?, peran = ? WHERE id_pengguna = ?";
        } else {
            sql = "UPDATE pengguna SET username = ?, nama_lengkap = ?, peran = ? WHERE id_pengguna = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, user.getUsername());
            if (updatePassword) {
                ps.setString(idx++, user.getPasswordHash());
            }
            ps.setString(idx++, user.getNamaLengkap());
            ps.setString(idx++, user.getPeran().name());
            ps.setInt(idx, user.getIdPengguna());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM pengguna WHERE id_pengguna = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Pengguna mapResultSetToPengguna(ResultSet rs) throws SQLException {
        Pengguna p = new Pengguna();
        p.setIdPengguna(rs.getInt("id_pengguna"));
        p.setUsername(rs.getString("username"));
        p.setPasswordHash(rs.getString("password_hash"));
        p.setNamaLengkap(rs.getString("nama_lengkap"));
        p.setPeran(PeranPengguna.fromString(rs.getString("peran")));
        Timestamp ts = rs.getTimestamp("dibuat_pada");
        if (ts != null) {
            p.setDibuatPada(ts.toLocalDateTime());
        }
        return p;
    }
}
