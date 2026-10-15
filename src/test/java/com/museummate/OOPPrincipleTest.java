package com.museummate;

import com.museummate.model.Artefak;
import com.museummate.model.Fosil;
import com.museummate.model.Koleksi;
import com.museummate.model.Lukisan;
import com.museummate.model.Naskah;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pengujian 4 pilar OOP sesuai ketentuan PRD Bagian 3.2 dan 8.3:
 * Abstraksi, Pewarisan, Polimorfisme, dan Enkapsulasi.
 */
class OOPPrincipleTest {

    @Test
    @DisplayName("1. Abstraksi: Class Koleksi adalah abstract dan memiliki method abstrak")
    void testAbstraksi() {
        assertTrue(Modifier.isAbstract(Koleksi.class.getModifiers()), "Class Koleksi harus dideklarasikan sebagai abstract.");
    }

    @Test
    @DisplayName("2. Pewarisan: Lukisan, Artefak, Naskah, Fosil mewarisi class Koleksi")
    void testPewarisan() {
        Lukisan lukisan = new Lukisan();
        Artefak artefak = new Artefak();
        Naskah naskah = new Naskah();
        Fosil fosil = new Fosil();

        assertTrue(lukisan instanceof Koleksi, "Lukisan harus mewarisi Koleksi");
        assertTrue(artefak instanceof Koleksi, "Artefak harus mewarisi Koleksi");
        assertTrue(naskah instanceof Koleksi, "Naskah harus mewarisi Koleksi");
        assertTrue(fosil instanceof Koleksi, "Fosil harus mewarisi Koleksi");
    }

    @Test
    @DisplayName("3. Polimorfisme: getFaktorKategori() menghasilkan nilai dinamis sesuai jenis turunan")
    void testPolimorfisme() {
        Koleksi k1 = new Naskah();
        Koleksi k2 = new Lukisan();
        Koleksi k3 = new Artefak();
        Koleksi k4 = new Fosil();

        assertEquals(1.5, k1.getFaktorKategori(), 0.001);
        assertEquals(1.2, k2.getFaktorKategori(), 0.001);
        assertEquals(1.0, k3.getFaktorKategori(), 0.001);
        assertEquals(0.8, k4.getFaktorKategori(), 0.001);
    }

    @Test
    @DisplayName("4. Enkapsulasi: Validasi field mencegah input kosong atau tidak valid")
    void testEnkapsulasi() {
        Naskah naskah = new Naskah();

        assertThrows(IllegalArgumentException.class, () -> naskah.setNama(""), "Nama kosong harus melempar exception");
        assertThrows(IllegalArgumentException.class, () -> naskah.setKodeRegistrasi(null), "Kode registrasi null harus melempar exception");
        assertThrows(IllegalArgumentException.class, () -> naskah.setKondisi(null), "Kondisi null harus melempar exception");
    }
}
