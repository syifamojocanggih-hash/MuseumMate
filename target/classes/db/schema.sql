-- ====================================================================
-- MuseumMate Database Schema
-- Sistem Manajemen Koleksi dan Konservasi Museum dengan Prioritas Otomatis
-- ====================================================================

CREATE DATABASE IF NOT EXISTS museummate_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE museummate_db;

-- 1. Tabel Pengguna (Admin & Konservator)
CREATE TABLE IF NOT EXISTS pengguna (
    id_pengguna INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nama_lengkap VARCHAR(100) NOT NULL,
    peran ENUM('ADMIN', 'KONSERVATOR') NOT NULL,
    dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Tabel Induk Koleksi (Abstraksi OOP: Koleksi)
CREATE TABLE IF NOT EXISTS koleksi (
    id_koleksi INT AUTO_INCREMENT PRIMARY KEY,
    kode_registrasi VARCHAR(50) NOT NULL UNIQUE,
    nama VARCHAR(150) NOT NULL,
    kategori ENUM('LUKISAN', 'ARTEFAK', 'NASKAH', 'FOSIL') NOT NULL,
    asal VARCHAR(100),
    tahun_perkiraan VARCHAR(50),
    bahan VARCHAR(100),
    lokasi_penyimpanan VARCHAR(100),
    sumber_perolehan VARCHAR(100),
    kondisi ENUM('BAIK', 'CUKUP', 'RUSAK_RINGAN', 'RUSAK_BERAT') NOT NULL DEFAULT 'BAIK',
    tanggal_diterima DATE NOT NULL,
    dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    diperbarui_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_kategori (kategori),
    INDEX idx_kondisi (kondisi)
) ENGINE=InnoDB;

-- 3. Tabel Atribut Khusus Lukisan (Pewarisan: Lukisan extends Koleksi)
CREATE TABLE IF NOT EXISTS koleksi_lukisan (
    id_koleksi INT PRIMARY KEY,
    seniman VARCHAR(100),
    teknik_media VARCHAR(100),
    dimensi VARCHAR(50),
    ada_bingkai BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Tabel Atribut Khusus Artefak (Pewarisan: Artefak extends Koleksi)
CREATE TABLE IF NOT EXISTS koleksi_artefak (
    id_koleksi INT PRIMARY KEY,
    era_dinasti VARCHAR(100),
    fungsi_objek VARCHAR(100),
    inskripsi_ornamen TEXT,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Tabel Atribut Khusus Naskah (Pewarisan: Naskah extends Koleksi)
CREATE TABLE IF NOT EXISTS koleksi_naskah (
    id_koleksi INT PRIMARY KEY,
    bahasa VARCHAR(50),
    aksara VARCHAR(50),
    alas_naskah VARCHAR(50),
    jumlah_halaman INT DEFAULT 1,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Tabel Atribut Khusus Fosil (Pewarisan: Fosil extends Koleksi)
CREATE TABLE IF NOT EXISTS koleksi_fosil (
    id_koleksi INT PRIMARY KEY,
    nama_ilmiah_taksa VARCHAR(100),
    bagian_anatomi VARCHAR(100),
    formasi_geologi VARCHAR(100),
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Tabel Pemeriksaan dan Riwayat Perawatan
CREATE TABLE IF NOT EXISTS pemeriksaan (
    id_pemeriksaan INT AUTO_INCREMENT PRIMARY KEY,
    id_koleksi INT NOT NULL,
    id_pengguna INT NOT NULL,
    tanggal_pemeriksaan DATE NOT NULL,
    kondisi_hasil ENUM('BAIK', 'CUKUP', 'RUSAK_RINGAN', 'RUSAK_BERAT') NOT NULL,
    tindakan VARCHAR(200) NOT NULL,
    catatan TEXT,
    dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE,
    FOREIGN KEY (id_pengguna) REFERENCES pengguna(id_pengguna),
    INDEX idx_pemeriksaan_tgl (tanggal_pemeriksaan)
) ENGINE=InnoDB;

-- 8. Tabel Jadwal Perawatan
CREATE TABLE IF NOT EXISTS jadwal_perawatan (
    id_jadwal INT AUTO_INCREMENT PRIMARY KEY,
    id_koleksi INT NOT NULL,
    tanggal_rencana DATE NOT NULL,
    kegiatan VARCHAR(150) NOT NULL,
    keterangan TEXT,
    status ENUM('DIRENCANAKAN', 'SELESAI', 'DIBATALKAN') NOT NULL DEFAULT 'DIRENCANAKAN',
    dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    diperbarui_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE,
    INDEX idx_jadwal_status (status, tanggal_rencana)
) ENGINE=InnoDB;

-- 9. Tabel Log Audit Aktivitas Pengguna (PRD F10 / UC-13)
CREATE TABLE IF NOT EXISTS log_aktivitas (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    id_pengguna INT,
    jenis_aksi VARCHAR(50) NOT NULL,
    tabel_terkait VARCHAR(50) NOT NULL,
    keterangan TEXT NOT NULL,
    waktu TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pengguna) REFERENCES pengguna(id_pengguna) ON DELETE SET NULL,
    INDEX idx_log_waktu (waktu)
) ENGINE=InnoDB;

