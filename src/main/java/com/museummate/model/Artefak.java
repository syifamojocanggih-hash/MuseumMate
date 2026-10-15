package com.museummate.model;

import java.time.LocalDate;

/**
 * Representasi koleksi kategori Artefak sejarah/arkeologi.
 * Mewarisi Koleksi dan meng-override method polimorfik.
 */
public class Artefak extends Koleksi {
    private String eraDinasti;
    private String fungsiObjek;
    private String inskripsiOrnamen;

    public Artefak() {
        super();
        setKategori(KategoriKoleksi.ARTEFAK);
    }

    public Artefak(String kodeRegistrasi, String nama, String asal, String tahunPerkiraan,
                   String bahan, String lokasiPenyimpanan, String sumberPerolehan,
                   KondisiKoleksi kondisi, LocalDate tanggalDiterima,
                   String eraDinasti, String fungsiObjek, String inskripsiOrnamen) {
        super(kodeRegistrasi, nama, KategoriKoleksi.ARTEFAK, asal, tahunPerkiraan, bahan,
              lokasiPenyimpanan, sumberPerolehan, kondisi, tanggalDiterima);
        this.eraDinasti = eraDinasti;
        this.fungsiObjek = fungsiObjek;
        this.inskripsiOrnamen = inskripsiOrnamen;
    }

    @Override
    public double getFaktorKategori() {
        return KategoriKoleksi.ARTEFAK.getFaktor(); // 1.0
    }

    @Override
    public int getIntervalPemeriksaanHari() {
        return 180; // Rekomendasi 6 bulan sekali
    }

    @Override
    public String getDeskripsiKhusus() {
        StringBuilder sb = new StringBuilder();
        if (eraDinasti != null && !eraDinasti.isEmpty()) sb.append("Era: ").append(eraDinasti).append(" | ");
        if (fungsiObjek != null && !fungsiObjek.isEmpty()) sb.append("Fungsi: ").append(fungsiObjek);
        return sb.toString();
    }

    public String getEraDinasti() {
        return eraDinasti != null ? eraDinasti : "";
    }

    public void setEraDinasti(String eraDinasti) {
        this.eraDinasti = eraDinasti != null ? eraDinasti.trim() : "";
    }

    public String getFungsiObjek() {
        return fungsiObjek != null ? fungsiObjek : "";
    }

    public void setFungsiObjek(String fungsiObjek) {
        this.fungsiObjek = fungsiObjek != null ? fungsiObjek.trim() : "";
    }

    public String getInskripsiOrnamen() {
        return inskripsiOrnamen != null ? inskripsiOrnamen : "";
    }

    public void setInskripsiOrnamen(String inskripsiOrnamen) {
        this.inskripsiOrnamen = inskripsiOrnamen != null ? inskripsiOrnamen.trim() : "";
    }
}
