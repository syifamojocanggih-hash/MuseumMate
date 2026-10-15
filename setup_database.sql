-- ====================================================================
-- MuseumMate - Skrip Lengkap Pembuatan Basis Data & Data Awal
-- Dapat langsung diimpor melalui HeidiSQL, Laragon, phpMyAdmin, atau MySQL CLI
-- ====================================================================

CREATE DATABASE IF NOT EXISTS museummate_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE museummate_db;

-- 1. Tabel Pengguna
CREATE TABLE IF NOT EXISTS pengguna (
    id_pengguna INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nama_lengkap VARCHAR(100) NOT NULL,
    peran ENUM('ADMIN', 'KONSERVATOR') NOT NULL,
    dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Tabel Induk Koleksi
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

-- 3. Tabel Atribut Khusus Lukisan
CREATE TABLE IF NOT EXISTS koleksi_lukisan (
    id_koleksi INT PRIMARY KEY,
    seniman VARCHAR(100),
    teknik_media VARCHAR(100),
    dimensi VARCHAR(50),
    ada_bingkai BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Tabel Atribut Khusus Artefak
CREATE TABLE IF NOT EXISTS koleksi_artefak (
    id_koleksi INT PRIMARY KEY,
    era_dinasti VARCHAR(100),
    fungsi_objek VARCHAR(100),
    inskripsi_ornamen TEXT,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Tabel Atribut Khusus Naskah
CREATE TABLE IF NOT EXISTS koleksi_naskah (
    id_koleksi INT PRIMARY KEY,
    bahasa VARCHAR(50),
    aksara VARCHAR(50),
    alas_naskah VARCHAR(50),
    jumlah_halaman INT DEFAULT 1,
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Tabel Atribut Khusus Fosil
CREATE TABLE IF NOT EXISTS koleksi_fosil (
    id_koleksi INT PRIMARY KEY,
    nama_ilmiah_taksa VARCHAR(100),
    bagian_anatomi VARCHAR(100),
    formasi_geologi VARCHAR(100),
    FOREIGN KEY (id_koleksi) REFERENCES koleksi(id_koleksi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Tabel Pemeriksaan
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

-- SEED DATA
-- Default password: admin123 untuk admin, konservator123 untuk konservator
INSERT INTO pengguna (id_pengguna, username, password_hash, nama_lengkap, peran) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ahmad Syifa (Administrator)', 'ADMIN'),
(2, 'konservator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Amanda Eka (Konservator Senior)', 'KONSERVATOR')
ON DUPLICATE KEY UPDATE nama_lengkap = VALUES(nama_lengkap);

INSERT INTO koleksi (id_koleksi, kode_registrasi, nama, kategori, asal, tahun_perkiraan, bahan, lokasi_penyimpanan, sumber_perolehan, kondisi, tanggal_diterima) VALUES
(1, 'MM-NSK-001', 'Naskah Kuno Babad Tanah Jawi', 'NASKAH', 'Surakarta, Jawa Tengah', 'Abad ke-18', 'Kertas Daluwang', 'Ruang Khusus Naskah - Rak A1', 'Hibah Keraton', 'RUSAK_RINGAN', '2024-01-10'),
(2, 'MM-LUK-001', 'Penangkapan Pangeran Diponegoro (Koleksi Reproduksi)', 'LUKISAN', 'Jawa Tengah', 'Tahun 1857', 'Cat Minyak di atas Kanvas', 'Galeri Utama - Dinding Timur', 'Pembelian Dinas Kebudayaan', 'BAIK', '2024-05-15'),
(3, 'MM-FOS-001', 'Fosil Tempurung Tengkorak Sangiran 17', 'FOSIL', 'Situs Sangiran, Jawa Tengah', 'Plestosen Tengah (± 700.000 thn)', 'Tulang Terfosilisasi', 'Ruang Paleontologi - Vitrin 03', 'Ekskavasi Arkeologi', 'RUSAK_BERAT', '2023-08-20'),
(4, 'MM-ART-001', 'Keris Pusaka Pamor Beras Wutah', 'ARTEFAK', 'Trowulan, Mojokerto', 'Abad ke-14 (Majapahit)', 'Besi Meteorit & Kayu Cendana', 'Ruang Senjata Pusaka - Vitrin 01', 'Sumbangan Kolektor', 'CUKUP', '2023-11-01'),
(5, 'MM-NSK-002', 'Serat Centhini Pupuh Sinom', 'NASKAH', 'Yogyakarta', 'Awal Abad ke-19', 'Daun Lontar', 'Ruang Arsip Suhu Terkendali', 'Hibah Paguyuban Budaya', 'RUSAK_BERAT', '2023-03-12'),
(6, 'MM-LUK-002', 'Pemandangan Lereng Gunung Merapi', 'LUKISAN', 'Muntilan, Magelang', 'Tahun 1930', 'Cat Minyak & Panel Kayu', 'Galeri Lanskap Nusantara - Dinding B', 'Sita Pelestarian Aset', 'CUKUP', '2024-02-18'),
(7, 'MM-ART-002', 'Arca Wisnu Berkendara Garuda', 'ARTEFAK', 'Singasari, Malang', 'Abad ke-13', 'Batu Andesit Hitam', 'Selasar Arca Selatan', 'Ekskavasi Balai Arkeologi', 'BAIK', '2024-06-01'),
(8, 'MM-FOS-002', 'Fragmen Rahang Bawah Stegodon Trigonocephalus', 'FOSIL', 'Trinil, Ngawi', 'Plestosen Awal (± 1,2 Juta thn)', 'Mineral Batuan & Kalsium Fosil', 'Ruang Paleontologi - Vitrin 07', 'Temuan Masyarakat Trinil', 'RUSAK_RINGAN', '2024-01-25'),
(9, 'MM-NSK-003', 'Kakawin Sutasoma Sloka Perdamaian', 'NASKAH', 'Bali / Jawa Timur', 'Abad ke-14', 'Lempengan Daun Lontar', 'Ruang Khusus Naskah - Rak B2', 'Penitipan Lembaga Adat', 'BAIK', '2024-07-10'),
(10, 'MM-ART-003', 'Prasasti Batu Bertulis Huruf Jawa Kuno', 'ARTEFAK', 'Lamongan, Jawa Timur', 'Abad ke-11 (Airlangga)', 'Batu Kapur Padat', 'Selasar Epigrafi Utama', 'Penyelamatan Cagar Budaya', 'RUSAK_RINGAN', '2023-09-15'),
(11, 'MM-LUK-003', 'Perjuangan Pemuda Arek Suroboyo 1945', 'LUKISAN', 'Surabaya', 'Tahun 1952', 'Cat Minyak di atas Kanvas', 'Galeri Perjuangan - Sisi Barat', 'Pemberian Legiun Veteran', 'RUSAK_BERAT', '2023-10-10'),
(12, 'MM-FOS-003', 'Gigi Geraham Gajah Purba Elephas Hysudrindicus', 'FOSIL', 'Bojonegoro', 'Plestosen Akhir', 'Enamel Gigi Terbatu', 'Ruang Paleontologi - Vitrin 09', 'Ekskavasi Mandiri', 'BAIK', '2024-04-05')
ON DUPLICATE KEY UPDATE nama = VALUES(nama);

INSERT INTO koleksi_lukisan (id_koleksi, seniman, teknik_media, dimensi, ada_bingkai) VALUES
(2, 'Gaya Raden Saleh Sjarif Boestaman', 'Minyak di atas kanvas linen', '125 x 90 cm', TRUE),
(6, 'Sudjojono (Atribusi)', 'Minyak di atas hardboard kayu', '80 x 60 cm', TRUE),
(11, 'Kussudiardja & Murid', 'Minyak ekspresionis di atas kanvas', '160 x 110 cm', TRUE)
ON DUPLICATE KEY UPDATE seniman = VALUES(seniman);

INSERT INTO koleksi_artefak (id_koleksi, era_dinasti, fungsi_objek, inskripsi_ornamen) VALUES
(4, 'Kerajaan Majapahit Akhir (Abad XIV)', 'Senjata Tikam Ritual & Perlambang Martabat', 'Kinatah emas motif floral pada pangkal bilah gandik'),
(7, 'Kerajaan Singasari (Abad XIII)', 'Arca Pemujaan Candi Dewa Wisnu', 'Hiasan mahkota Kirita-makuta bertatah teratai'),
(10, 'Era Kerajaan Medang Kamulan / Kahuripan', 'Pemberitahuan Status Tanah Perdikan (Sima)', 'Inskripsi 18 baris aksara Kawi berbahasa Jawa Kuno')
ON DUPLICATE KEY UPDATE era_dinasti = VALUES(era_dinasti);

INSERT INTO koleksi_naskah (id_koleksi, bahasa, aksara, alas_naskah, jumlah_halaman) VALUES
(1, 'Jawa Kuno / Kawi', 'Carakan Jawa Mataram', 'Kertas Tradisional Daluwang', 184),
(5, 'Jawa Madya', 'Aksara Jawa Pegon & Kawi', 'Lontar Terikat Tali Sutra', 76),
(9, 'Sansekerta & Kawi', 'Aksara Bali Kuno', 'Lontar Pengasih', 42)
ON DUPLICATE KEY UPDATE bahasa = VALUES(bahasa);

INSERT INTO koleksi_fosil (id_koleksi, nama_ilmiah_taksa, bagian_anatomi, formasi_geologi) VALUES
(3, 'Homo erectus paleojavanicus', 'Cranial calvaria (tempurung dahi ke oksipital)', 'Formasi Kabuh Pucangan Bawah'),
(8, 'Stegodon trigonocephalus florensis', 'Mandibula kanan bergeligi molar M2-M3', 'Lapisan Vulkanik Purba Bengawan Solo'),
(12, 'Elephas hysudrindicus', 'Dens molaris superior rahang atas', 'Formasi Notopuro Ngandong')
ON DUPLICATE KEY UPDATE nama_ilmiah_taksa = VALUES(nama_ilmiah_taksa);

INSERT INTO pemeriksaan (id_koleksi, id_pengguna, tanggal_pemeriksaan, kondisi_hasil, tindakan, catatan) VALUES
(1, 2, DATE_SUB(CURDATE(), INTERVAL 90 DAY), 'RUSAK_RINGAN', 'Pembersihan debu kering & penggantian silika gel', 'Ditemukan serbuk rayap kayu pada sudut tepi lembaran halaman 40-52.'),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'BAIK', 'Inspeksi visual permukaan pernis & tegangan kanvas', 'Lapisan vernis stabil, kelembaban ruangan 55% terjaga baik.'),
(3, 2, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'RUSAK_BERAT', 'Aplikasi konsolidan Paraloid B-72 3%', 'Muncul retakan mikro baru pada tepi foramen magnum, perlu penyangga busa EVA baru.'),
(4, 2, DATE_SUB(CURDATE(), INTERVAL 120 DAY), 'CUKUP', 'Pengolesan minyak warangan & cengkeh', 'Oksidasi tipis pada gonjo bilah, warangka kayu aman dari jamur.'),
(5, 2, DATE_SUB(CURDATE(), INTERVAL 150 DAY), 'RUSAK_BERAT', 'Fumigasi tertutup dalam chamber thymol', 'Daun lontar getas dan mengalami kerapuhan pada 12 lempir awal.'),
(6, 2, DATE_SUB(CURDATE(), INTERVAL 75 DAY), 'CUKUP', 'Pembersihan debu permukaan mikrofiber', 'Frame kayu mulai kendur sambungannya, cat minyak stabil.'),
(7, 2, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'BAIK', 'Pembersihan spora lumut kering', 'Batu andesit kokoh dan stabil di atas dudukan beton.'),
(8, 2, DATE_SUB(CURDATE(), INTERVAL 45 DAY), 'RUSAK_RINGAN', 'Penetralan garam kalsium & stabilisasi resin', 'Garam kapur muncul di celah akar geraham karena fluktuasi RH.'),
(10, 2, DATE_SUB(CURDATE(), INTERVAL 180 DAY), 'RUSAK_RINGAN', 'Sikat nilon halus & pembersihan endapan tanah', 'Huruf kawi di baris 10-14 mengalami abrasi cuaca.'),
(11, 2, DATE_SUB(CURDATE(), INTERVAL 100 DAY), 'RUSAK_BERAT', 'Pemasangan facing kertas jepang pada cat mengelupas', 'Flaking cat parah pada bagian tengah figur bendera merah putih.'),
(12, 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'BAIK', 'Inspeksi berkala vitrin tertutup', 'Bebas dari jamur dan getaran mekanik.');

INSERT INTO jadwal_perawatan (id_koleksi, tanggal_rencana, kegiatan, keterangan, status) VALUES
(5, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Konsolidasi Serat Lontar dengan Etanol & Gliserin', 'Telah melewati batas tanggal rencana, prioritas perawatan mendesak.', 'DIRENCANAKAN'),
(1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'Laminasi Kertas Jepang Kering pada Babad Tanah Jawi', 'Pengerjaan di ruang laboratorium konservasi steril.', 'DIRENCANAKAN'),
(3, DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'Pembuatan Dudukan Penyangga Akrilik 3D Vitrin Fosil', 'Pencegahan stres mekanis pada dinding foramen tengkorak.', 'DIRENCANAKAN'),
(11, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'Infill Warna Reversibel pada Lukisan Pemuda 1945', 'Menggunakan cat air konservasi Winsor & Newton.', 'DIRENCANAKAN'),
(2, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Pembersihan Permukaan Kanvas Lukisan Diponegoro', 'Selesai tepat waktu oleh petugas konservator.', 'SELESAI'),
(7, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'Aplikasi Biocide Ramah Lingkungan Arca Wisnu', 'Pembersihan spora mikro berhasil 100%.', 'SELESAI');
