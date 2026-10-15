package com.museummate;

import com.museummate.model.Artefak;
import com.museummate.model.Fosil;
import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.Lukisan;
import com.museummate.model.Naskah;
import com.museummate.service.PrioritasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pengujian unit rumus kalkulasi prioritas otomatis sesuai PRD Bagian 3.2 dan 6.4:
 * Skor Prioritas = (Bobot Kondisi x 10) + (Hari sejak Pemeriksaan Terakhir / 30) x Faktor Kategori
 */
class PrioritasRumusTest {

    private PrioritasService prioritasService;
    private LocalDate tanggalSekarang;

    @BeforeEach
    void setUp() {
        prioritasService = new PrioritasService();
        tanggalSekarang = LocalDate.of(2026, 10, 15);
    }

    @Test
    @DisplayName("PRD Contoh 1: Naskah Rusak Ringan diperiksa 90 hari lalu menghasilkan skor 34.5")
    void testContoh1NaskahRusakRingan() {
        Naskah naskah = new Naskah();
        naskah.setKodeRegistrasi("MM-NSK-001");
        naskah.setNama("Naskah Kuno Babad Tanah Jawi");
        naskah.setKondisi(KondisiKoleksi.RUSAK_RINGAN); // Bobot 3

        LocalDate tglPemeriksaan = tanggalSekarang.minusDays(90); // 90 hari lalu
        double skor = prioritasService.hitungSkor(naskah, tglPemeriksaan, tanggalSekarang);

        // (3 * 10) + (90 / 30) * 1.5 = 30 + (3 * 1.5) = 34.5
        assertEquals(34.5, skor, 0.001, "Skor naskah harus tepat 34.5");
    }

    @Test
    @DisplayName("PRD Contoh 2: Lukisan Baik diperiksa 30 hari lalu menghasilkan skor 11.2")
    void testContoh2LukisanBaik() {
        Lukisan lukisan = new Lukisan();
        lukisan.setKodeRegistrasi("MM-LUK-001");
        lukisan.setNama("Penangkapan Pangeran Diponegoro");
        lukisan.setKondisi(KondisiKoleksi.BAIK); // Bobot 1

        LocalDate tglPemeriksaan = tanggalSekarang.minusDays(30); // 30 hari lalu
        double skor = prioritasService.hitungSkor(lukisan, tglPemeriksaan, tanggalSekarang);

        // (1 * 10) + (30 / 30) * 1.2 = 10 + (1 * 1.2) = 11.2
        assertEquals(11.2, skor, 0.001, "Skor lukisan harus tepat 11.2");
    }

    @Test
    @DisplayName("Fosil Rusak Berat diperiksa 60 hari lalu menghasilkan skor 41.6")
    void testFosilRusakBerat() {
        Fosil fosil = new Fosil();
        fosil.setKodeRegistrasi("MM-FOS-001");
        fosil.setNama("Fosil Sangiran 17");
        fosil.setKondisi(KondisiKoleksi.RUSAK_BERAT); // Bobot 4

        LocalDate tglPemeriksaan = tanggalSekarang.minusDays(60); // 60 hari lalu
        double skor = prioritasService.hitungSkor(fosil, tglPemeriksaan, tanggalSekarang);

        // (4 * 10) + (60 / 30) * 0.8 = 40 + (2 * 0.8) = 41.6
        assertEquals(41.6, skor, 0.001);
    }

    @Test
    @DisplayName("Artefak Cukup diperiksa 120 hari lalu menghasilkan skor 24.0")
    void testArtefakCukup() {
        Artefak artefak = new Artefak();
        artefak.setKodeRegistrasi("MM-ART-001");
        artefak.setNama("Keris Majapahit");
        artefak.setKondisi(KondisiKoleksi.CUKUP); // Bobot 2

        LocalDate tglPemeriksaan = tanggalSekarang.minusDays(120); // 120 hari lalu
        double skor = prioritasService.hitungSkor(artefak, tglPemeriksaan, tanggalSekarang);

        // (2 * 10) + (120 / 30) * 1.0 = 20 + 4.0 = 24.0
        assertEquals(24.0, skor, 0.001);
    }

    @Test
    @DisplayName("Koleksi belum pernah diperiksa menggunakan tanggal diterima sebagai acuan hitung")
    void testFallbackTanggalDiterima() {
        Naskah naskah = new Naskah();
        naskah.setKodeRegistrasi("MM-NSK-NEW");
        naskah.setNama("Naskah Baru");
        naskah.setKondisi(KondisiKoleksi.BAIK); // Bobot 1
        naskah.setTanggalDiterima(tanggalSekarang.minusDays(60)); // Diterima 60 hari lalu

        // null tgl pemeriksaan -> fallback ke tanggal diterima
        double skor = prioritasService.hitungSkor(naskah, null, tanggalSekarang);

        // (1 * 10) + (60 / 30) * 1.5 = 10 + 3.0 = 13.0
        assertEquals(13.0, skor, 0.001);
    }

    @Test
    @DisplayName("Urutan prioritas: Benda kritis tampil lebih dahulu daripada benda berkondisi baik")
    void testPerbandinganUrutanPrioritas() {
        Naskah mendesak = new Naskah();
        mendesak.setKodeRegistrasi("MM-1");
        mendesak.setNama("Naskah Kritis");
        mendesak.setKondisi(KondisiKoleksi.RUSAK_RINGAN);
        double skorMendesak = mendesak.hitungSkorPrioritas(tanggalSekarang.minusDays(90), tanggalSekarang); // 34.5

        Lukisan aman = new Lukisan();
        aman.setKodeRegistrasi("MM-2");
        aman.setNama("Lukisan Aman");
        aman.setKondisi(KondisiKoleksi.BAIK);
        double skorAman = aman.hitungSkorPrioritas(tanggalSekarang.minusDays(30), tanggalSekarang); // 11.2

        assertTrue(skorMendesak > skorAman, "Koleksi mendesak harus memperoleh skor lebih tinggi.");
    }
}
