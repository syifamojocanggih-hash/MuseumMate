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
                
                // Pastikan tabel log_aktivitas (PRD F10) dibuat jika database sudah ada sebelumnya
                ResultSet rsLog = stmt.executeQuery("SHOW TABLES LIKE 'log_aktivitas'");
                if (!rsLog.next()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS log_aktivitas (" +
                            "id_log INT AUTO_INCREMENT PRIMARY KEY, " +
                            "id_pengguna INT, " +
                            "jenis_aksi VARCHAR(50) NOT NULL, " +
                            "tabel_terkait VARCHAR(50) NOT NULL, " +
                            "keterangan TEXT NOT NULL, " +
                            "waktu TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (id_pengguna) REFERENCES pengguna(id_pengguna) ON DELETE SET NULL, " +
                            "INDEX idx_log_waktu (waktu)" +
                            ") ENGINE=InnoDB;");
                    stmt.executeUpdate("INSERT INTO log_aktivitas (id_pengguna, jenis_aksi, tabel_terkait, keterangan, waktu) VALUES " +
                            "(1, 'LOGIN', 'pengguna', 'Administrator Ahmad Syifa berhasil masuk ke sistem MuseumMate.', DATE_SUB(NOW(), INTERVAL 5 DAY)), " +
                            "(1, 'TAMBAH', 'koleksi', 'Pendaftaran master koleksi SIOLA-ART-001 s.d SIOLA-FOS-145 dari dokumen kuratorial Siola.', DATE_SUB(NOW(), INTERVAL 5 DAY)), " +
                            "(1, 'TAMBAH', 'jadwal_perawatan', 'Menambahkan jadwal perawatan untuk SIOLA-ART-046 (Grand Piano 1895).', DATE_SUB(NOW(), INTERVAL 4 DAY)), " +
                            "(2, 'LOGIN', 'pengguna', 'Konservator Amanda Eka berhasil masuk ke sistem MuseumMate.', DATE_SUB(NOW(), INTERVAL 3 DAY)), " +
                            "(2, 'TAMBAH', 'pemeriksaan', 'Mencatat hasil inspeksi fisik berkala koleksi SIOLA-ART-001 (Kondisi Rusak Ringan).', DATE_SUB(NOW(), INTERVAL 2 DAY)), " +
                            "(1, 'UBAH', 'jadwal_perawatan', 'Memperbarui rincian kegiatan perawatan SIOLA-ART-087 ke status Terencana.', DATE_SUB(NOW(), INTERVAL 1 DAY));");
                }
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
