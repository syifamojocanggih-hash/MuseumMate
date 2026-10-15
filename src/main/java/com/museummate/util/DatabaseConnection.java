package com.museummate.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manajer koneksi JDBC ke basis data MySQL (Laragon / Standalone).
 * Dilengkapi dengan auto-initialization skema tabel dan data seed jika belum ada.
 */
public class DatabaseConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DB_NAME = "museummate_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String BASE_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    private static final String DB_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";

    private static boolean initialized = false;

    /**
     * Memperoleh koneksi aktif ke basis data museummate_db.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initializeDatabase();
            initialized = true;
        }
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    /**
     * Menguji ketersediaan layanan basis data.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Membuat basis data dan mengeksekusi skema tabel jika belum tersedia.
     */
    private static synchronized void initializeDatabase() {
        // 1. Buat database jika belum ada
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            System.err.println("Peringatan saat inisialisasi basis data: " + e.getMessage());
        }

        // 2. Periksa apakah tabel pengguna sudah ada
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'koleksi'");
            boolean tablesExist = rs.next();

            if (!tablesExist) {
                System.out.println("Tabel belum ditemukan. Menjalankan skrip inisialisasi schema.sql & seed_data.sql...");
                executeSqlScript(conn, "/db/schema.sql");
                executeSqlScript(conn, "/db/seed_data.sql");
                System.out.println("Inisialisasi basis data berhasil diselesaikan.");
            } else {
                // Pastikan akun bawaan memiliki hash BCrypt yang valid
                stmt.executeUpdate("UPDATE pengguna SET password_hash = '$2a$10$X5n4R/yuL10Mh.BtxxXctOO05edLPDZ6ofQDEZyBTxkLud2ObNzRC' WHERE username = 'admin'");
                stmt.executeUpdate("UPDATE pengguna SET password_hash = '$2a$10$EpwndmY6IelLsj9hUCFR1e9v5j7CvB2Ao/LJm0v0kVNO/Jek9EIbe' WHERE username = 'konservator'");
            }
        } catch (SQLException e) {
            System.err.println("Gagal memverifikasi skema tabel: " + e.getMessage());
        }
    }

    /**
     * Menjalankan file skrip SQL dari classpath resources.
     */
    public static void executeSqlScript(Connection conn, String resourcePath) {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("File resource SQL tidak ditemukan: " + resourcePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                        continue;
                    }
                    sqlBuilder.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String query = sqlBuilder.toString().trim();
                        query = query.substring(0, query.length() - 1).trim();
                        if (!query.isEmpty() && !query.toUpperCase().startsWith("USE") && !query.toUpperCase().startsWith("CREATE DATABASE")) {
                            try {
                                stmt.execute(query);
                            } catch (SQLException ex) {
                                // Abaikan error duplikasi seed
                                if (!ex.getMessage().contains("Duplicate")) {
                                    System.err.println("Eksekusi query SQL: " + ex.getMessage());
                                }
                            }
                        }
                        sqlBuilder.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Kesalahan saat menjalankan skrip SQL: " + e.getMessage());
        }
    }
}
