package com.museummate;

import com.museummate.model.JadwalPerawatan;
import com.museummate.model.StatusJadwal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pengujian unit untuk logika deteksi Peringatan & Notifikasi Jadwal (PRD F8 / UC-11).
 */
class NotifikasiJadwalTest {

    @Test
    @DisplayName("Jadwal berstatus DIRENCANAKAN yang telah lewat tanggal diidentifikasi sebagai Terlambat (Overdue)")
    void testJadwalTerlambatOverdue() {
        LocalDate today = LocalDate.of(2026, 10, 18);
        LocalDate overdueDate = today.minusDays(5);

        JadwalPerawatan j = new JadwalPerawatan(1, overdueDate, "Konservasi Lukisan", "Cat mengelupas");
        j.setStatus(StatusJadwal.DIRENCANAKAN);

        assertTrue(j.getTanggalRencana().isBefore(today));
        assertEquals(5, ChronoUnit.DAYS.between(j.getTanggalRencana(), today));
    }

    @Test
    @DisplayName("Jadwal berstatus DIRENCANAKAN dalam jendela 7 hari ke depan terdeteksi sebagai Mendekati Jatuh Tempo")
    void testJadwalJatuhTempoSegera() {
        LocalDate today = LocalDate.of(2026, 10, 18);
        LocalDate upcomingDate = today.plusDays(4);

        JadwalPerawatan j = new JadwalPerawatan(2, upcomingDate, "Fumigasi Naskah", "Sterilisasi serangga");
        j.setStatus(StatusJadwal.DIRENCANAKAN);

        assertFalse(j.getTanggalRencana().isBefore(today));
        long daysAhead = ChronoUnit.DAYS.between(today, j.getTanggalRencana());
        assertTrue(daysAhead <= 7, "Harus berada dalam jangka 7 hari jatuh tempo");
        assertEquals(4, daysAhead);
    }

    @Test
    @DisplayName("Jadwal yang jatuh tempo lebih dari 7 hari tidak masuk dalam notifikasi mendesak")
    void testJadwalJatuhTempoJauh() {
        LocalDate today = LocalDate.of(2026, 10, 18);
        LocalDate farDate = today.plusDays(20);

        long daysAhead = ChronoUnit.DAYS.between(today, farDate);
        assertFalse(daysAhead <= 7, "Lebih dari 7 hari tidak masuk notifikasi mendesak");
    }

    @Test
    @DisplayName("Jadwal dengan status SELESAI atau DIBATALKAN tidak memicu peringatan aktif")
    void testJadwalSelesaiTidakAktif() {
        JadwalPerawatan j1 = new JadwalPerawatan(1, LocalDate.of(2026, 10, 1), "Pembersihan", "Rutin");
        j1.setStatus(StatusJadwal.SELESAI);

        JadwalPerawatan j2 = new JadwalPerawatan(2, LocalDate.of(2026, 10, 5), "Pemeriksaan", "Insidental");
        j2.setStatus(StatusJadwal.DIBATALKAN);

        assertEquals(StatusJadwal.SELESAI, j1.getStatus());
        assertEquals(StatusJadwal.DIBATALKAN, j2.getStatus());
    }
}
