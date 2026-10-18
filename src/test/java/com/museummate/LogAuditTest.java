package com.museummate;

import com.museummate.model.LogAktivitas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pengujian unit untuk Fitur Log Audit (PRD F10 / UC-13).
 */
class LogAuditTest {

    @Test
    @DisplayName("Model LogAktivitas menginisialisasi waktu dan menyimpan atribut dengan benar (Enkapsulasi)")
    void testLogAktivitasInitialization() {
        LogAktivitas log = new LogAktivitas(1, "TAMBAH", "koleksi", "Menambahkan koleksi baru SIOLA-ART-001");

        assertEquals(1, log.getIdPengguna());
        assertEquals("TAMBAH", log.getJenisAksi());
        assertEquals("koleksi", log.getTabelTerkait());
        assertEquals("Menambahkan koleksi baru SIOLA-ART-001", log.getKeterangan());
        assertNotNull(log.getWaktu());
        assertNotNull(log.getWaktuFormatted());
    }

    @Test
    @DisplayName("LogAktivitas menampilkan fallback 'Sistem' saat idPengguna atau nama kosong")
    void testLogAktivitasFallbackNama() {
        LogAktivitas log = new LogAktivitas(null, "LOGIN", "auth", "Upaya login tanpa sesi");
        assertEquals("Sistem", log.getNamaPengguna());

        log.setUsernamePengguna("petugas1");
        assertEquals("petugas1", log.getNamaPengguna());

        log.setNamaPengguna("Budi Santoso");
        assertEquals("Budi Santoso", log.getNamaPengguna());
    }

    @Test
    @DisplayName("Format waktu log audit sesuai dengan pola dd/MM/yyyy HH:mm:ss")
    void testWaktuFormatting() {
        LocalDateTime fixedTime = LocalDateTime.of(2026, 10, 18, 14, 30, 45);
        LogAktivitas log = new LogAktivitas(10, 1, "Admin Siola", "admin", "UBAH", "jadwal_perawatan", "Perbarui jadwal", fixedTime);

        assertEquals("18/10/2026 14:30:45", log.getWaktuFormatted());
        assertTrue(log.toString().contains("18/10/2026 14:30:45"));
        assertTrue(log.toString().contains("UBAH"));
    }
}
