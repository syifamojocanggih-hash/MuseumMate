-- ====================================================================
-- MuseumMate Seed Data
-- Data Uji Awal Sesuai PRD (Bagian 6.4 & 10)
-- Password Default:
--   admin / admin123
--   konservator / konservator123
-- ====================================================================

USE museummate_db;

-- 1. Seed Pengguna
INSERT INTO pengguna (id_pengguna, username, password_hash, nama_lengkap, peran) VALUES
(1, 'admin', '$2a$10$X5n4R/yuL10Mh.BtxxXctOO05edLPDZ6ofQDEZyBTxkLud2ObNzRC', 'Ahmad Syifa (Administrator)', 'ADMIN'),
(2, 'konservator', '$2a$10$EpwndmY6IelLsj9hUCFR1e9v5j7CvB2Ao/LJm0v0kVNO/Jek9EIbe', 'Amanda Eka (Konservator Senior)', 'KONSERVATOR')
ON DUPLICATE KEY UPDATE nama_lengkap = VALUES(nama_lengkap);

-- 2. Seed Koleksi Induk
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

-- 3. Seed Detail Khusus Kategori
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

-- 4. Seed Riwayat Pemeriksaan Terkini
-- Catatan: Tanggal pemeriksaan diset relatif agar penghitungan prioritas akurat
INSERT INTO pemeriksaan (id_koleksi, id_pengguna, tanggal_pemeriksaan, kondisi_hasil, tindakan, catatan) VALUES
-- Koleksi 1 (Naskah Babad): Diperiksa ~90 hari lalu, kondisi Rusak Ringan
(1, 2, DATE_SUB(CURDATE(), INTERVAL 90 DAY), 'RUSAK_RINGAN', 'Pembersihan debu kering & penggantian silika gel', 'Ditemukan serbuk rayap kayu pada sudut tepi lembaran halaman 40-52.'),

-- Koleksi 2 (Lukisan Diponegoro): Diperiksa ~30 hari lalu, kondisi Baik
(2, 2, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'BAIK', 'Inspeksi visual permukaan pernis & tegangan kanvas', 'Lapisan vernis stabil, kelembaban ruangan 55% terjaga baik.'),

-- Koleksi 3 (Fosil Sangiran): Diperiksa ~60 hari lalu, kondisi Rusak Berat
(3, 2, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'RUSAK_BERAT', 'Aplikasi konsolidan Paraloid B-72 3%', 'Muncul retakan mikro baru pada tepi foramen magnum, perlu penyangga busa EVA baru.'),

-- Koleksi 4 (Keris Majapahit): Diperiksa ~120 hari lalu, kondisi Cukup
(4, 2, DATE_SUB(CURDATE(), INTERVAL 120 DAY), 'CUKUP', 'Pengolesan minyak warangan & cengkeh', 'Oksidasi tipis pada gonjo bilah, warangka kayu aman dari jamur.'),

-- Koleksi 5 (Serat Centhini): Diperiksa ~150 hari lalu, kondisi Rusak Berat
(5, 2, DATE_SUB(CURDATE(), INTERVAL 150 DAY), 'RUSAK_BERAT', 'Fumigasi tertutup dalam chamber thymol', 'Daun lontar getas dan mengalami kerapuhan pada 12 lempir awal.'),

-- Koleksi 6 (Lukisan Merapi): Diperiksa ~75 hari lalu, kondisi Cukup
(6, 2, DATE_SUB(CURDATE(), INTERVAL 75 DAY), 'CUKUP', 'Pembersihan debu permukaan mikrofiber', 'Frame kayu mulai kendur sambungannya, cat minyak stabil.'),

-- Koleksi 7 (Arca Wisnu): Diperiksa ~15 hari lalu, kondisi Baik
(7, 2, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'BAIK', 'Pembersihan spora lumut kering', 'Batu andesit kokoh dan stabil di atas dudukan beton.'),

-- Koleksi 8 (Rahang Stegodon): Diperiksa ~45 hari lalu, kondisi Rusak Ringan
(8, 2, DATE_SUB(CURDATE(), INTERVAL 45 DAY), 'RUSAK_RINGAN', 'Penetralan garam kalsium & stabilisasi resin', 'Garam kapur muncul di celah akar geraham karena fluktuasi RH.'),

-- Koleksi 10 (Prasasti Airlangga): Diperiksa ~180 hari lalu, kondisi Rusak Ringan
(10, 2, DATE_SUB(CURDATE(), INTERVAL 180 DAY), 'RUSAK_RINGAN', 'Sikat nilon halus & pembersihan endapan tanah', 'Huruf kawi di baris 10-14 mengalami abrasi cuaca.'),

-- Koleksi 11 (Lukisan Pemuda 1945): Diperiksa ~100 hari lalu, kondisi Rusak Berat
(11, 2, DATE_SUB(CURDATE(), INTERVAL 100 DAY), 'RUSAK_BERAT', 'Pemasangan facing kertas jepang pada cat mengelupas', 'Flaking cat parah pada bagian tengah figur bendera merah putih.'),

-- Koleksi 12 (Geraham Gajah Purba): Diperiksa ~20 hari lalu, kondisi Baik
(12, 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'BAIK', 'Inspeksi berkala vitrin tertutup', 'Bebas dari jamur dan getaran mekanik.');

-- 5. Seed Jadwal Perawatan
INSERT INTO jadwal_perawatan (id_koleksi, tanggal_rencana, kegiatan, keterangan, status) VALUES
-- Jadwal Terlambat (Overdue) untuk demonstrasi fitur F5
(5, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Konsolidasi Serat Lontar dengan Etanol & Gliserin', 'Telah melewati batas tanggal rencana, prioritas perawatan mendesak.', 'DIRENCANAKAN'),
-- Jadwal Mendatang
(1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'Laminasi Kertas Jepang Kering pada Babad Tanah Jawi', 'Pengerjaan di ruang laboratorium konservasi steril.', 'DIRENCANAKAN'),
(3, DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'Pembuatan Dudukan Penyangga Akrilik 3D Vitrin Fosil', 'Pencegahan stres mekanis pada dinding foramen tengkorak.', 'DIRENCANAKAN'),
(11, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'Infill Warna Reversibel pada Lukisan Pemuda 1945', 'Menggunakan cat air konservasi Winsor & Newton.', 'DIRENCANAKAN'),
-- Jadwal yang sudah Selesai
(2, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Pembersihan Permukaan Kanvas Lukisan Diponegoro', 'Selesai tepat waktu oleh petugas konservator.', 'SELESAI'),
(7, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'Aplikasi Biocide Ramah Lingkungan Arca Wisnu', 'Pembersihan spora mikro berhasil 100%.', 'SELESAI');
