package com.museummate;

import com.museummate.model.KategoriKoleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.TrenKondisiItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pengujian unit untuk kalkulasi & model Grafik Tren Kondisi Koleksi (PRD F9 / UC-12).
 */
class TrenKondisiTest {

    @Test
    @DisplayName("Model TrenKondisiItem menyimpan periode, kategori, skor rata-rata, dan jumlah pemeriksaan dengan benar")
    void testTrenKondisiItemModel() {
        TrenKondisiItem item = new TrenKondisiItem("2026-10", "Okt 2026", KategoriKoleksi.NASKAH, 2.5, 8);

        assertEquals("2026-10", item.getPeriode());
        assertEquals("Okt 2026", item.getLabelPeriode());
        assertEquals(KategoriKoleksi.NASKAH, item.getKategori());
        assertEquals(2.5, item.getSkorRataRata(), 0.001);
        assertEquals(8, item.getJumlahPemeriksaan());
    }

    @Test
    @DisplayName("Bobot kondisi sesuai dengan skala 1-4 PRD (Baik=1, Cukup=2, Rusak Ringan=3, Rusak Berat=4)")
    void testBobotKondisiSkala() {
        assertEquals(1, KondisiKoleksi.BAIK.getBobot());
        assertEquals(2, KondisiKoleksi.CUKUP.getBobot());
        assertEquals(3, KondisiKoleksi.RUSAK_RINGAN.getBobot());
        assertEquals(4, KondisiKoleksi.RUSAK_BERAT.getBobot());

        // Simulasi rata-rata 3 pemeriksaan: Baik(1), Cukup(2), Rusak Ringan(3) -> Rata-rata = 2.0
        double rataRata = (KondisiKoleksi.BAIK.getBobot() + KondisiKoleksi.CUKUP.getBobot() + KondisiKoleksi.RUSAK_RINGAN.getBobot()) / 3.0;
        assertEquals(2.0, rataRata, 0.001);

        // Nilai rata-rata berada dalam rentang valid 1.0 s.d 4.0
        assertTrue(rataRata >= 1.0 && rataRata <= 4.0);
    }
}
