# MuseumMate 🏛️

**Sistem Manajemen Koleksi dan Konservasi Museum dengan Prioritas Perawatan Otomatis**

> Proyek Praktikum Pemrograman Berbasis Objek (PBO) — D3 Teknik Informatika PSDKU Lamongan, PENS (2026)  
> **Team 8**: Amanda Eka Faraghita, Ahmad Syifa'ul Falakhul Khayyi, Aimatus Sholiha  
> **Dosen Pengampu**: Yunia Ikawati, S.ST., M.Tr.Kom.

---

## 📌 Ringkasan Produk
MuseumMate adalah aplikasi desktop berbasis Java (Java 21 LTS / JavaFX) yang dirancang untuk membantu pengelola museum mendata inventaris benda bersejarah, memantau kondisi fisik secara real-time, mengelola jadwal konservasi, dan **menghitung urutan prioritas perawatan secara otomatis**.

### Fitur Pembeda Utama (PRD Bagian 6.4)
Sistem secara dinamis menghitung skor urgensi setiap kali daftar dimuat tanpa menyimpan skor statis di basis data:
$$\text{Skor Prioritas} = (\text{Bobot Kondisi} \times 10) + \left(\frac{\text{Hari sejak Pemeriksaan Terakhir}}{30}\right) \times \text{Faktor Kategori}$$

- **Bobot Kondisi**: `Baik = 1`, `Cukup = 2`, `Rusak Ringan = 3`, `Rusak Berat = 4`
- **Faktor Kategori**: `Naskah = 1.5`, `Lukisan = 1.2`, `Artefak = 1.0`, `Fosil = 0.8`
- **Acuan jika belum pernah diperiksa**: Dihitung dari tanggal diterima.

---

## 🛠️ Prasyarat Lingkungan
1. **Java Development Kit (JDK)**: Java 21 LTS atau lebih tinggi (JDK 26 terdeteksi dan kompatibel).
2. **MySQL Database**: Laragon atau XAMPP (port default 3306).
3. **Maven**: Menggunakan Maven Wrapper bawaan proyek (`mvnw` / `mvnw.cmd`), tidak memerlukan instalasi manual tambahan.

---

## 🚀 Panduan Menjalankan Proyek

### 1. Menjalankan MySQL di Laragon
1. Buka aplikasi **Laragon** di komputer Anda.
2. Klik tombol **Start All** (pastikan modul MySQL aktif pada port `3306`).
3. *(Opsional)* Impor skrip [`setup_database.sql`](setup_database.sql) melalui HeidiSQL atau phpMyAdmin.  
   *(Catatan: Aplikasi juga memiliki fitur **auto-initialization**, di mana skema tabel dan data awal akan dibuat otomatis jika basis data belum ada saat pertama kali dijalankan).*

### 2. Menjalankan Aplikasi
Buka terminal PowerShell pada direktori proyek, lalu jalankan:

```powershell
.\mvnw javafx:run
```

Aplikasi akan otomatis mengompilasi kode dan membuka layar login desktop.

### 3. Akun Pengguna Bawaan (Demo)
Aplikasi membedakan hak akses berdasarkan dua peran (PRD F1):

| Peran | Username | Password | Wewenang Utama |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Kelola seluruh data koleksi, jadwal perawatan, kelola akun pengguna, ekspor laporan. |
| **Konservator** | `konservator` | `konservator123` | Catat hasil pemeriksaan berkala, lihat riwayat fisik, pantau daftar prioritas perawatan. |

---

## 🧪 Menjalankan Pengujian Unit (JUnit 5)
Untuk memvalidasi rumus kalkulasi prioritas PRD Bagian 6.4 dan 4 pilar OOP:

```powershell
.\mvnw test
```

Semua 10 pengujian unit akan dieksekusi:
- **`PrioritasRumusTest`**: Memverifikasi contoh perhitungan PRD (Naskah 34.5, Lukisan 11.2, Fosil 41.6, fallback tanggal diterima, dan urutan prioritas).
- **`OOPPrincipleTest`**: Memverifikasi Abstraksi, Pewarisan, Polimorfisme, dan Enkapsulasi.

---

### 10 Fitur Utama Sesuai Dokumen PRD Terbaru
1. **F1 (UC-01 & UC-05)**: Login & Pengelolaan Pengguna (Role-based access: Admin & Konservator, jBCrypt hash).
2. **F2 (UC-02 & UC-03)**: Manajemen Data Koleksi (CRUD koleksi, pencarian real-time, filter kategori & kondisi).
3. **F3 (bagian dari UC-03)**: Pengelompokan Kategori Berbasis OOP (Lukisan, Artefak, Naskah, Fosil dengan atribut khusus).
4. **F4 (UC-06 & UC-07)**: Pencatatan Pemeriksaan & Riwayat Fisik (Auto-update kondisi koleksi).
5. **F5 (UC-04)**: Jadwal Perawatan Konservasi (Rencana tindakan, status DIRENCANAKAN, SELESAI, DIBATALKAN).
6. **F6 (UC-08 & UC-09)**: Prioritas Perawatan Otomatis (Perhitungan skor urgensi dinamis berbasis bobot & faktor kerentanan).
7. **F7 (UC-10)**: Dashboard & Laporan Statistik (Metrik ringkasan, sebaran kategori & kondisi, ekspor PDF).
8. **F8 (UC-11)**: Peringatan & Notifikasi Jadwal Perawatan (Deteksi jadwal terlambat / overdue dan jadwal jatuh tempo dalam 7 hari).
9. **F9 (UC-12)**: Grafik Tren Kondisi Koleksi (Visualisasi perubahan kondisi fisik dari waktu ke waktu per kategori).
10. **F10 (UC-13)**: Log Audit Aktivitas Sistem (Pencatatan riwayat tambah, ubah, hapus data dan login ke tabel `log_aktivitas` untuk pengawasan dan akuntabilitas).

---

## 🏗️ Struktur Arsitektur & Penerapan OOP

```
MuseumMate/
├── pom.xml                               # Konfigurasi Maven & dependensi
├── mvnw & mvnw.cmd                       # Maven Wrapper lintas platform
├── setup_database.sql                    # Skrip DDL & seed data uji awal (termasuk log_aktivitas)
├── DESIGN.md                             # Pedoman sistem desain & warna
├── GEMINI.md                             # Konfigurasi antislop & panduan agen
└── src/
    ├── main/
    │   ├── java/com/museummate/
    │   │   ├── MainApp.java              # Titik masuk utama JavaFX
    │   │   ├── model/                    # Domain Model & OOP Hierarchy
    │   │   │   ├── Koleksi.java          # [Abstraksi] Abstract class induk
    │   │   │   ├── Lukisan.java          # [Pewarisan & Polimorfisme] Faktor 1.2
    │   │   │   ├── Artefak.java          # [Pewarisan & Polimorfisme] Faktor 1.0
    │   │   │   ├── Naskah.java           # [Pewarisan & Polimorfisme] Faktor 1.5
    │   │   │   ├── Fosil.java            # [Pewarisan & Polimorfisme] Faktor 0.8
    │   │   │   ├── Pengguna.java         # [Enkapsulasi] Akun & peran
    │   │   │   ├── Pemeriksaan.java      # Riwayat inspeksi fisik
    │   │   │   ├── JadwalPerawatan.java  # Jadwal rencana & status keterlambatan
    │   │   │   ├── PrioritasItem.java    # DTO antarmuka antrian prioritas
    │   │   │   ├── LogAktivitas.java     # [F10] DTO jejak audit aktivitas sistem
    │   │   │   └── TrenKondisiItem.java  # [F9] DTO agregasi tren kondisi berkala
    │   │   ├── dao/                      # Lapisan Akses Basis Data (JDBC)
    │   │   │   ├── KoleksiDAO.java       # Polimorfik CRUD koleksi
    │   │   │   ├── PenggunaDAO.java      # CRUD akun & peran
    │   │   │   ├── PemeriksaanDAO.java   # Auto-update kondisi koleksi & data tren
    │   │   │   ├── JadwalPerawatanDAO.java # Query jadwal, overdue, & upcoming 7 hari
    │   │   │   └── LogAktivitasDAO.java  # [F10] CRUD riwayat audit log
    │   │   ├── service/                  # Logika Bisnis
    │   │   │   ├── AuthService.java      # jBCrypt login verifier & audit logger
    │   │   │   ├── PrioritasService.java # Engine hitung skor PRD 6.4
    │   │   │   ├── KoleksiService.java   # Validasi data & kode unik
    │   │   │   ├── PemeriksaanService.java
    │   │   │   ├── JadwalService.java    # Notifikasi jadwal terlambat & mendatang
    │   │   │   ├── DashboardService.java # Agregasi statistik, metrik, & tren
    │   │   │   ├── LogAktivitasService.java # [F10] Service audit logger otomatis
    │   │   │   └── LaporanService.java   # Generator berkas PDF OpenPDF
    │   │   ├── controller/               # JavaFX Event Handlers
    │   │   │   ├── DashboardController.java # Dashboard metrik, notifikasi banner, & LineChart
    │   │   │   ├── KoleksiController.java
    │   │   │   ├── KoleksiFormController.java
    │   │   │   ├── PrioritasController.java
    │   │   │   ├── PemeriksaanController.java
    │   │   │   ├── CatatPemeriksaanController.java
    │   │   │   ├── JadwalController.java
    │   │   │   ├── TambahJadwalController.java
    │   │   │   ├── NotifikasiController.java # [F8] Controller modul notifikasi jadwal
    │   │   │   ├── LogAuditController.java   # [F10] Controller modul log audit
    │   │   │   ├── PenggunaController.java
    │   │   │   └── MainLayoutController.java
    │   │   └── util/                     # DatabaseConnection, PasswordUtil, AlertHelper, DateUtil
    │   └── resources/
    │       ├── com/museummate/
    │       │   ├── view/                 # FXML layouts (termasuk notifikasi & log_audit)
    │       │   └── css/style.css         # Desain tampilan berstandar museum
    │       └── db/                       # schema.sql & seed_data.sql
    └── test/
        └── java/com/museummate/          # Unit tests JUnit 5 (19 pengujian)
```

---

## 📑 Ekspor Berkas Laporan (PDF)
Aplikasi mendukung ekspor laporan langsung berformat PDF resmi:
1. **Laporan Inventaris Koleksi**: Daftar seluruh data koleksi museum beserta atribut dan lokasi simpan.
2. **Laporan Prioritas Konservasi**: Daftar antrian perawatan terurut dari skor tertinggi untuk tindak lanjut petugas di laboratorium konservasi.




