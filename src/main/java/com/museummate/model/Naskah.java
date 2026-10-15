package com.museummate.model;

import java.time.LocalDate;

/**
 * Representasi koleksi kategori Naskah Kuno/Manuskrip.
 * Mewarisi Koleksi dan meng-override method polimorfik.
 * Naskah memiliki faktor kerentanan tertinggi (1.5) karena bahan organik yang rapuh.
 */
public class Naskah extends Koleksi {
    private String bahasa;
    private String aksara;
    private String alasNaskah;
    private int jumlahHalaman;

    public Naskah() {
        super();
        setKategori(KategoriKoleksi.NASKAH);
        this.jumlahHalaman = 1;
    }

    public Naskah(String kodeRegistrasi, String nama, String asal, String tahunPerkiraan,
                  String bahan, String lokasiPenyimpanan, String sumberPerolehan,
                  KondisiKoleksi kondisi, LocalDate tanggalDiterima,
                  String bahasa, String aksara, String alasNaskah, int jumlahHalaman) {
        super(kodeRegistrasi, nama, KategoriKoleksi.NASKAH, asal, tahunPerkiraan, bahan,
              lokasiPenyimpanan, sumberPerolehan, kondisi, tanggalDiterima);
        this.bahasa = bahasa;
        this.aksara = aksara;
        this.alasNaskah = alasNaskah;
        setJumlahHalaman(jumlahHalaman);
    }

    @Override
    public double getFaktorKategori() {
        return KategoriKoleksi.NASKAH.getFaktor(); // 1.5
    }

    @Override
    public int getIntervalPemeriksaanHari() {
        return 60; // Naskah rapuh, diperiksa minimal 2 bulan sekali
    }

    @Override
    public String getDeskripsiKhusus() {
        StringBuilder sb = new StringBuilder();
        if (bahasa != null && !bahasa.isEmpty()) sb.append("Bahasa: ").append(bahasa).append(" | ");
        if (aksara != null && !aksara.isEmpty()) sb.append("Aksara: ").append(aksara).append(" | ");
        if (alasNaskah != null && !alasNaskah.isEmpty()) sb.append("Media: ").append(alasNaskah).append(" | ");
        sb.append(jumlahHalaman).append(" hlm");
        return sb.toString();
    }

    public String getBahasa() {
        return bahasa != null ? bahasa : "";
    }

    public void setBahasa(String bahasa) {
        this.bahasa = bahasa != null ? bahasa.trim() : "";
    }

    public String getAksara() {
        return aksara != null ? aksara : "";
    }

    public void setAksara(String aksara) {
        this.aksara = aksara != null ? aksara.trim() : "";
    }

    public String getAlasNaskah() {
        return alasNaskah != null ? alasNaskah : "";
    }

    public void setAlasNaskah(String alasNaskah) {
        this.alasNaskah = alasNaskah != null ? alasNaskah.trim() : "";
    }

    public int getJumlahHalaman() {
        return jumlahHalaman;
    }

    public void setJumlahHalaman(int jumlahHalaman) {
        this.jumlahHalaman = Math.max(1, jumlahHalaman);
    }
}
