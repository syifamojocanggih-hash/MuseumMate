# 🏛️ PANDUAN MASTER PRESENTASI & MATRIKS STATUS PRD MUSEUMMATE
### Sistem Manajemen Koleksi dan Konservasi Museum dengan Prioritas Perawatan Otomatis
**Tingkat Kesiapan UAS: ~95% (Seluruh Kategori Must Have & Should Have Selesai 100%)**

> **Mata Kuliah**: Praktikum Pemrograman Berbasis Objek (PBO)  
> **Program Studi**: D3 Teknik Informatika PSDKU Lamongan — Politeknik Elektronika Negeri Surabaya (PENS)  
> **Tahun Akademik**: 2026  
> **Dosen Pengampu**: Yunia Ikawati, S.ST., M.Tr.Kom.  
> **Team 8**:
> 1. Amanda Eka Faraghita (NRP: 3125521031)
> 2. Ahmad Syifa'ul Falakhul Khayyi (NRP: 3125521037)
> 3. Aimatus Sholiha (NRP: 3125521045)

---

## 1. Ringkasan Eksekutif & Latar Belakang (The "Why")

### Lokus Riset & Studi Kasus Konkret
* **Lokasi**: Museum Surabaya, Lantai 1 Gedung Siola (Jl. Tunjungan No. 1–3, Genteng, Surabaya).
* **Pengelola**: UPTD Pengelolaan Museum dan Gedung Seni Budaya, Disbudporapar Kota Surabaya.
* **Status Cagar Budaya**: SK Walikota Surabaya Nomor 188.45/251/436.1.2/2011.
* **Volume Data Riil**: Memiliki lebih dari 1.000 benda bersejarah, dengan **336 spesimen master konkret** yang telah diintegrasikan langsung ke dalam database MuseumMate (mulai dari brankas kolonial Whiteaway Laidlaw 1877, becak masa peralihan, mobil damkar 1972, hingga arsip Resolusi Jihad).

### Masalah Pengelolaan Konvensional
1. **Pencatatan Terpisah-pisah**: Data inventaris umumnya tersimpan di buku registrasi manual atau file spreadsheet yang rawan korup dan tidak sinkron.
2. **Kebutaan Prioritas (*Priority Blindness*)**: Sumber daya konservator (waktu kerja, tenaga ahli kimiawi, serta anggaran bahan pengawet) sangat terbatas. Selama ini tidak ada instrumen objektif untuk menentukan *"Benda mana yang harus diselamatkan terlebih dahulu hari ini?"*
3. **Jadwal Konservasi Kerap Terlewat**: Pemeliharaan sering kali bersifat kuratif (baru ditangani saat artefak sudah terlanjur lapuk, berjamur, atau rapuh parah), bukan preventif terjadwal.
4. **Ketiadaan Jejak Audit (*Audit Trail*)**: Tidak adanya rekaman digital tentang siapa yang mengubah status koleksi atau melakukan tindakan pembersihan, sehingga akuntabilitas pemeliharaan cagar budaya sulit dipantau.

---

## 2. Hasil Riset & Analisis Kerentanan Material

Berdasarkan kajian kuratorial dan konservasi cagar budaya tropis, laju degradasi benda dipengaruhi oleh jenis materialnya:
* **Naskah Kuno (Faktor Kerentanan 1.5)**: Bahan organik kertas daluang, lontar, atau serat kayu sangat sensitif terhadap asam, jamur, dan kelembapan udara. Memiliki risiko kerusakan tertinggi.
* **Lukisan (Faktor Kerentanan 1.2)**: Lapisan cat minyak, pernis, dan kain kanvas organik rentan mengalami retak seribu (*craquelure*), kendur, atau serangan spora jamur.
* **Artefak (Faktor Kerentanan 1.0)**: Benda tiga dimensi logam, kayu jati, keramik, atau kendaraan bersejarah memiliki ketahanan material sedang.
* **Fosil (Faktor Kerentanan 0.8)**: Mineral batuan dan fragmen anatomi purba membatu memiliki struktur fisik yang paling padat dan stabil dibanding material organik.

---

## 3. Solusi Utama: MuseumMate

### A. Algoritma Prioritas Perawatan Otomatis (PRD Bagian 6.4)
Sistem menghitung skor urgensi secara dinamis (*on-the-fly*) setiap kali antrean dibuka, tanpa menyimpan skor statis yang cepat usang di database:

$$\text{Skor Prioritas} = (\text{Bobot Kondisi} \times 10) + \left(\frac{\text{Hari sejak Pemeriksaan Terakhir}}{30}\right) \times \text{Faktor Kategori}$$

* **Bobot Kondisi**: `Baik = 1`, `Cukup = 2`, `Rusak Ringan = 3`, `Rusak Berat = 4`
* **Faktor Kategori**: `Naskah = 1.5`, `Lukisan = 1.2`, `Artefak = 1.0`, `Fosil = 0.8`
* **Aturan Khusus**: Jika koleksi belum pernah diperiksa, acuan waktu dihitung dari `tanggal_diterima`.
* **Klasifikasi Urgensi**:
  * **Kritis**: Skor $\ge 40$ (Badge Merah — Tindakan Darurat)
  * **Tinggi**: $30 \le \text{Skor} < 40$ (Badge Oranye — Jadwalkan Segera)
  * **Sedang**: $20 \le \text{Skor} < 30$ (Badge Kuning — Pemantauan Rutin)
  * **Rendah**: $\text{Skor} < 20$ (Badge Hijau — Kondisi Stabil)

### B. Penerapan 4 Pilar Pemrograman Berorientasi Objek (OOP)
1. **Abstraksi**: Abstract class induk `Koleksi` memuat atribut umum dan metode abstrak `hitungFaktorKategori()` serta `getRekomendasiTindakan()`.
2. **Pewarisan (Inheritance)**: Subclass `Lukisan`, `Artefak`, `Naskah`, dan `Fosil` mewarisi `Koleksi` dan memperluas atribut spesifik masing-masing (misal: seniman/bingkai di Lukisan, aksara/halaman di Naskah, taksa/formasi di Fosil).
3. **Polimorfisme (Polymorphism)**: Masing-masing subclass meng-override metode penentuan faktor risiko dan instruksi penanganan kimiawi yang presisi.
4. **Enkapsulasi (Encapsulation)**: Proteksi atribut menggunakan akses `private` dengan setter/getter yang divalidasi ketat (misal pencegahan kondisi null, validasi tanggal, dan proteksi hash kata sandi).

---

## 4. Matriks Perbandingan: Status PRD vs Implementasi Aktual

Berikut adalah audit kesesuaian antara dokumen spesifikasi produk (**PRD_MuseumMate.docx**) dengan implementasi aktual pada basis kode:

| ID Fitur / Kebutuhan | Kategori PRD | Status Implementasi | Bukti Kode & Catatan Teknis |
|---|:---:|:---:|---|
| **F1 — Login & Multi-Role** | **Must Have** | **✅ 100% Selesai** | `AuthService.java` & `LoginController.java`. Otentikasi dua peran: Administrator & Konservator dengan password ter-hash `jBCrypt`. |
| **Diferensiasi Dua Peran (Admin vs Konservator)** | **Must Have** | **✅ 100% Selesai** | `MainLayoutController.java`, `KoleksiController.java`, `JadwalController.java`. Menu Pengguna/Audit Log dan tombol Tambah/Ubah/Hapus disembunyikan untuk Konservator. |
| **F2 — Manajemen Koleksi (CRUD)** | **Must Have** | **✅ 100% Selesai** | `KoleksiDAO.java`, `KoleksiService.java`, `KoleksiController.java`. Mendukung CRUD lengkap, pencarian real-time via `FilteredList`, dan filter kategori. |
| **Validasi Kode Registrasi Unik** | **Must Have** | **✅ 100% Selesai** | Divalidasi di 3 lapisan: batasan `UNIQUE` di MySQL, validasi logika di `KoleksiService.java`, dan dialog penolakan di `KoleksiFormController.java`. |
| **Konfirmasi Hapus Koleksi Ber-riwayat** | **Must Have** | **✅ 100% Selesai** | `KoleksiController.java` baris 186-201. Menghitung jumlah riwayat fisik; jika ada riwayat, memunculkan dialog konfirmasi bertingkat. |
| **F3 — Pengelompokan Kategori OOP** | **Must Have** | **✅ 100% Selesai** | `KoleksiFormController.java`. Subform input berubah dinamis sesuai kategori (Lukisan, Artefak, Naskah, Fosil). |
| **F4 — Pencatatan Pemeriksaan & Riwayat** | **Must Have** | **✅ 100% Selesai** | `CatatPemeriksaanController.java`, `PemeriksaanDAO.java`. Tiap entri inspeksi otomatis memperbarui kondisi terkini pada master koleksi. |
| **Validasi Tanggal Tidak di Masa Depan** | **Must Have** | **✅ 100% Selesai** | Divalidasi di 3 tingkat (`CatatPemeriksaanController.java`, `PemeriksaanService.java`, dan `Pemeriksaan.java`) menggunakan `isAfter(LocalDate.now())`. |
| **F5 — Jadwal Perawatan Konservasi** | **Should Have** | **✅ 100% Selesai** | `JadwalController.java`, `JadwalPerawatanDAO.java`. Pemantauan status agenda konservasi (DIRENCANAKAN, SELESAI, DIBATALKAN). |
| **Update Status Jadwal Manual** | **Should Have** | **✅ 100% Selesai** | Tersedia tombol *Tandai Selesai* dan *Batalkan Jadwal* di UI `JadwalController.java` lengkap dengan pencatatan ke log audit. |
| **F6 — Prioritas Perawatan Otomatis** | **Must Have** | **✅ 100% Selesai** | `PrioritasService.java`. Rumus dinamis sesuai PRD Bagian 6.4 (bobot kondisi x 10 + hari/30 x faktor kategori; fallback tanggal diterima jika belum pernah diperiksa). |
| **F7 — Dashboard & Metrik Statistik** | **Should Have** | **✅ 100% Selesai** | `DashboardController.java`. 4 kartu statistik utama, Hero Image Carousel otomatis 4 detik, dan ringkasan agenda konservasi. |
| **F8 — Notifikasi & Peringatan Dini** | **Should Have** | **✅ 100% Selesai** | `NotifikasiController.java`, `JadwalService.java`. Deteksi otomatis jadwal *overdue* (terlambat) dan jadwal jatuh tempo dalam 7 hari mendatang. |
| **F9 — Grafik Tren Kondisi Koleksi** | **Should Have** | **✅ 100% Selesai** | `DashboardController.java` & `DashboardService.java`. Visualisasi `LineChart` JavaFX kondisi fisik rata-rata per kategori dari waktu ke waktu. |
| **F10 — Log Audit Aktivitas Sistem** | **Should Have** | **✅ 100% Selesai** | `LogAktivitasService.java` & `LogAuditController.java`. Pencatatan otomatis setiap aksi penambahan, perubahan, penghapusan, dan sesi login. |
| **Ekspor Laporan Resmi (PDF)** | **Could Have** | **✅ 100% Selesai** | `LaporanService.java`. Ekspor Dokumen Inventaris dan Antrean Prioritas Konservasi ke format PDF via OpenPDF (Fitur *Could Have* yang sukses diselesaikan). |
| **Keamanan: Hashing Password** | **Non-Fungsional** | **✅ 100% Selesai** | `PasswordUtil.java` (jBCrypt 0.4 Blowfish) menjamin tidak ada kata sandi yang tersimpan dalam bentuk plain text. |
| **Penanganan Error Validasi Tanpa Crash** | **Non-Fungsional** | **✅ 100% Selesai** | Seluruh form memakai `AlertHelper` terpusat; duplikasi data atau input kosong ditangani dengan pesan ramah, tanpa crash aplikasi. |
| **Metrik Pengujian Unit (JUnit 5)** | **Kriteria Uji** | **✅ 100% Selesai** | 19 skenario pengujian unit di `src/test/java/` lulus 100% (`BUILD SUCCESS`), termasuk validasi rumus skor prioritas pada angka contoh PRD. |
| **Histori Perubahan Skor Prioritas** | **Could Have** | **ℹ️ Ditiadakan** | **Keputusan Desain**: Skor dihitung murni dinamis/real-time (*on-the-fly*) sesuai PRD 6.4 agar tidak usang (*stale*). Digantikan oleh Grafik Tren Kondisi Fisik Riil. |

---

## 5. Resolusi Pertanyaan Terbuka PRD (Bagian 11 PRD)

Pada dokumen PRD lama (Bagian 11), terdapat 4 pertanyaan terbuka yang belum diputuskan. Berikut adalah **jawaban final dan status implementasi** yang telah diselesaikan tim untuk disampaikan kepada dosen pengampu:

1. **Apakah bobot kondisi dan faktor kategori pada Bagian 6.4 perlu dikonsultasikan?**
   * **Keputusan Tim**: Bobot kondisi (1–4) dan faktor risiko material (Naskah 1.5, Lukisan 1.2, Artefak 1.0, Fosil 0.8) telah difinalkan dan divalidasi keabsahan logikanya melalui 6 skenario pengujian unit otomatis di `PrioritasRumusTest.java`.
2. **Apakah laporan perlu diekspor ke PDF pada versi UAS?**
   * **Keputusan Tim**: **YA, SUDAH DIIKSEKUSI**. Fitur ekspor PDF menggunakan library OpenPDF telah selesai dibuat di `LaporanService.java` untuk Laporan Inventaris dan Laporan Antrean Konservasi.
3. **Apakah dua peran (Admin, Konservator) dipertahankan atau disederhanakan?**
   * **Keputusan Tim**: **DIPERTAHANKAN SECARA PENUH**. Tim telah berhasil menerapkan pemisahan hak akses nyata (RBAC) pada menu dan aksi tombol di `MainLayoutController`, `KoleksiController`, dan `JadwalController`.
4. **Apakah dibutuhkan akun contoh/demo bawaan untuk mempercepat presentasi?**
   * **Keputusan Tim**: **SUDAH TERSEDIA**. Disediakan akun bawaan siap pakai (`admin`/`admin123` dan `konservator`/`konservator123`) lengkap dengan data awal 336 koleksi riil Museum Siola.

---

## 6. Skenario & Naskah Step-by-Step Demonstrasi di Hadapan Dosen

Ikuti alur berikut saat demonstrasi di depan dosen penguji agar seluruh use case (UC-01 s.d. UC-13) tercover secara terstruktur:

### Skenario 1: Autentikasi & Pemisahan Hak Akses (UC-01 & UC-05)
1. Buka aplikasi via terminal: `.\mvnw javafx:run`.
2. Masuk pertama kali menggunakan akun Konservator: `konservator` / `konservator123`.
3. Tunjukkan ke dosen: menu **Kelola Akun** dan **Log Audit** pada sidebar otomatis **tidak muncul**.
4. Buka menu **Data Koleksi**: perlihatkan bahwa tombol Tambah, Ubah, dan Hapus Koleksi disembunyikan. Konservator difokuskan untuk tugas teknis konservasi.
5. Logout, lalu masuk sebagai Administrator: `admin` / `admin123`. Seluruh menu administrasi kini terbuka penuh.

### Skenario 2: Dashboard & Metrik Pemantauan (UC-10 & UC-12)
1. Tunjukkan **Hero Image Carousel interaktif**: slide foto pameran asli Siola berganti secara otomatis tiap 4 detik dengan efek fade-slide halus.
2. Tunjukkan **4 Kartu Metrik**: Total 336 Koleksi, Koleksi Kritis/Tinggi, Jadwal Terlambat, dan Kondisi Prima.
3. Jelaskan **Grafik Tren Kondisi Koleksi**: grafik garis yang membaca riwayat pemeriksaan fisik antar 4 kategori dari waktu ke waktu.
4. Tunjukkan tombol **Notifikasi Bel** di navbar atas yang mendeteksi jadwal jatuh tempo atau terlambat.

### Skenario 3: Penerapan OOP & Manajemen Koleksi (UC-02 & UC-03)
1. Buka menu **Data Koleksi**.
2. Demonstrasikan pencarian real-time: ketik *"Becak"* atau *"Brankas"* di kotak pencarian, tabel merespons seketika (< 0.1 detik).
3. Klik tombol **Tambah Koleksi**:
   * Tunjukkan saat memilih kategori **Naskah**, formulir meminta input *Bahasa, Aksara, Alas Naskah, dan Jumlah Halaman*.
   * Ganti kategori menjadi **Lukisan**, formulir langsung berganti meminta *Seniman, Teknik Media, Dimensi, dan Bingkai*.
   * *Point to Highlight*: Jelaskan kepada dosen bahwa ini adalah implementasi nyata dari konsep **Abstraksi, Pewarisan, dan Polimorfisme OOP**.
4. Uji validasi kode unik: coba masukkan kode registrasi yang sudah ada, tunjukkan bahwa sistem menolak penyimpanan secara elegan tanpa crash.

### Skenario 4: Fitur Unggulan — Prioritas Perawatan Otomatis (UC-08 & UC-09)
1. Buka menu **Prioritas Perawatan**.
2. Jelaskan rumus PRD Bagian 6.4 di hadapan dosen:
   * Tunjukkan koleksi dengan badge merah **KRITIS** di urutan teratas (misalnya naskah kuno yang rusak dan sudah lama tidak diperiksa).
   * Tunjukkan bahwa skor dihitung secara dinamis (*on-the-fly*).
3. Klik tombol **Cetak Laporan PDF**: tunjukkan berkas PDF resmi antrean konservasi yang langsung terbuka rapi berstandar cetak.

### Skenario 5: Pemeriksaan Fisik & Siklus Jadwal (UC-04, UC-06, UC-07)
1. Buka menu **Catat Pemeriksaan**.
2. Pilih salah satu koleksi yang rusak, catat hasil pemeriksaan baru menjadi **BAIK** dengan tindakan pembersihan kimiawi.
3. Uji validasi tanggal: coba pilih tanggal besok/masa depan, tunjukkan sistem menolak dengan pesan *"Tanggal pemeriksaan tidak boleh di masa depan"*.
4. Simpan: tunjukkan bahwa status master koleksi langsung ter-update otomatis.
5. Buka menu **Jadwal Perawatan**: tunjukkan daftar agenda konservasi, lalu ubah status agenda menggunakan tombol **Tandai Selesai** atau **Batalkan Jadwal**.

### Skenario 6: Jejak Akuntabilitas — Log Audit (UC-13)
1. Buka menu **Log Audit**.
2. Perlihatkan kepada dosen bahwa seluruh aksi yang baru saja didemokan (tambah koleksi, pemeriksaan baru, dan ubah status jadwal) tercatat secara otomatis lengkap dengan *timestamp* detik dan nama pengguna yang melakukannya.
3. Tutup dengan menjalankan pengujian unit: `.\mvnw test` di terminal untuk memperlihatkan **19 Unit Test JUnit 5 Lolos 100% (`BUILD SUCCESS`)**.
