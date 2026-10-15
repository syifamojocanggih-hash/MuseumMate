package com.museummate.model;

import java.time.LocalDate;

/**
 * Representasi koleksi kategori Lukisan.
 * Mewarisi Koleksi dan meng-override method polimorfik.
 */
public class Lukisan extends Koleksi {
    private String seniman;
    private String teknikMedia;
    private String dimensi;
    private boolean adaBingkai;

    public Lukisan() {
        super();
        setKategori(KategoriKoleksi.LUKISAN);
        this.adaBingkai = true;
    }

    public Lukisan(String kodeRegistrasi, String nama, String asal, String tahunPerkiraan,
                   String bahan, String lokasiPenyimpanan, String sumberPerolehan,
                   KondisiKoleksi kondisi, LocalDate tanggalDiterima,
                   String seniman, String teknikMedia, String dimensi, boolean adaBingkai) {
        super(kodeRegistrasi, nama, KategoriKoleksi.LUKISAN, asal, tahunPerkiraan, bahan,
              lokasiPenyimpanan, sumberPerolehan, kondisi, tanggalDiterima);
        this.seniman = seniman;
        this.teknikMedia = teknikMedia;
        this.dimensi = dimensi;
        this.adaBingkai = adaBingkai;
    }

    @Override
    public double getFaktorKategori() {
        return KategoriKoleksi.LUKISAN.getFaktor(); // 1.2
    }

    @Override
    public int getIntervalPemeriksaanHari() {
        return 90; // Rekomendasi 3 bulan sekali
    }

    @Override
    public String getDeskripsiKhusus() {
        StringBuilder sb = new StringBuilder();
        if (seniman != null && !seniman.isEmpty()) sb.append("Seniman: ").append(seniman).append(" | ");
        if (teknikMedia != null && !teknikMedia.isEmpty()) sb.append("Media: ").append(teknikMedia).append(" | ");
        if (dimensi != null && !dimensi.isEmpty()) sb.append("Dimensi: ").append(dimensi);
        return sb.toString();
    }

    public String getSeniman() {
        return seniman != null ? seniman : "";
    }

    public void setSeniman(String seniman) {
        this.seniman = seniman != null ? seniman.trim() : "";
    }

    public String getTeknikMedia() {
        return teknikMedia != null ? teknikMedia : "";
    }

    public void setTeknikMedia(String teknikMedia) {
        this.teknikMedia = teknikMedia != null ? teknikMedia.trim() : "";
    }

    public String getDimensi() {
        return dimensi != null ? dimensi : "";
    }

    public void setDimensi(String dimensi) {
        this.dimensi = dimensi != null ? dimensi.trim() : "";
    }

    public boolean isAdaBingkai() {
        return adaBingkai;
    }

    public void setAdaBingkai(boolean adaBingkai) {
        this.adaBingkai = adaBingkai;
    }
}
