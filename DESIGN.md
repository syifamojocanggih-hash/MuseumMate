# MuseumMate Design System & Direction

Dokumen ini mendefinisikan identitas visual, tone antarmuka, dan standar kualitas desain untuk aplikasi desktop **MuseumMate**.

## 1. Identitas & Karakter Produk
MuseumMate adalah alat kerja profesional bagi kurator, konservator, dan pengelola museum. Karakter desain mengusung nuansa **Modern Curatorial Workstation (Sleek Slate & Royal Sapphire)**:
- **Tone**: Bersih, presisi, berwibawa, modern, dan sejuk (sekelas software desktop profesional modern).
- **Fokus UX**: Keterbacaan data historis tinggi, tabel data dengan *breathing space* yang lega, scrollbar ultra-slim minimalis, dan visualisasi prioritas konservasi yang langsung terbaca jelas.

## 2. Parameter Desain (Dials)
- **ENERGY**: 2 (Tenang, fungsional, berfokus pada data koleksi museum).
- **RHYTHM**: 2 (Tata letak teratur antara kartu ringkasan, tabel data responsif, dan panel inspeksi).
- **MOTION**: 1 (Transisi halus pada hover status dan pemuatan tampilan, tanpa animasi bergerak yang mengganggu konsentrasi).

## 3. Palet Warna (WCAG AA Compliant)
- **Primary / Accent**: `#2563eb` (Royal Sapphire Blue) - Hover `#1d4ed8`, Active `#1e40af`.
- **Secondary / Slate**: `#0b0f19` (Obsidian Dark Sidebar) & `#111827` (Card Box).
- **Surface**: `#f8fafc` (Canvas Stone Light) dan `#ffffff` (Card background dengan soft ambient drop shadow).
- **Border**: `#e2e8f0` (Subtle boundary line) & `#cbd5e1` (Input boundary).
- **Text Primary**: `#0f172a` (Kontras 13.5:1 terhadap latar putih).
- **Text Muted**: `#64748b` (Kontras 5.2:1 terhadap latar putih).

### Indikator Status & Urgensi Prioritas (Modern Soft Pill Badges)
- **Kritis / Rusak Berat**: Background `#fee2e2`, Border `#fca5a5`, Teks `#991b1b` (Red-800).
- **Tinggi / Rusak Ringan**: Background `#ffedd5`, Border `#fdba74`, Teks `#9a3412` (Orange-800).
- **Sedang / Cukup**: Background `#dbeafe`, Border `#93c5fd`, Teks `#1e40af` (Blue-800).
- **Rendah / Kondisi Baik**: Background `#dcfce7`, Border `#86efac`, Teks `#166534` (Emerald-800).

## 4. Tipografi
- Font: `Segoe UI`, `Inter`, -apple-system, sans-serif.
- Hierarki:
  - Title H1: 21px Bold
  - Section H2: 15px Bold
  - Body / Table: 13px Regular (Cell height 38px untuk kenyamanan mata)
  - Caption / Tag: 11px SemiBold

## 5. Standar Kerajinan (Craftsmanship)
- Scrollbar custom ultra-slim (8px) tanpa panah jadul 90-an.
- Setiap tombol memiliki fungsi nyata dan hover state yang responsif.
- Navigasi menyesuaikan hak akses (Admin vs. Konservator).
- Status kosong (empty state), status kalkulasi, dan dialog konfirmasi tertangani dengan jelas.
