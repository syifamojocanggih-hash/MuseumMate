package com.museummate.model;

import java.time.LocalDate;

/**
 * Representasi koleksi kategori Fosil/Paleontologi.
 * Mewarisi Koleksi dan meng-override method polimorfik.
 */
public class Fosil extends Koleksi {
    private String namaIlmiahTaksa;
    private String bagianAnatomi;
    private String formasiGeologi;

    public Fosil() {
        super();
        setKategori(KategoriKoleksi.FOSIL);
    }

    public Fosil(String kodeRegistrasi, String nama, String asal, String tahunPerkiraan,
                 String bahan, String lokasiPenyimpanan, String sumberPerolehan,
                 KondisiKoleksi kondisi, LocalDate tanggalDiterima,
                 String namaIlmiahTaksa, String bagianAnatomi, String formasiGeologi) {
        super(kodeRegistrasi, nama, KategoriKoleksi.FOSIL, asal, tahunPerkiraan, bahan,
              lokasiPenyimpanan, sumberPerolehan, kondisi, tanggalDiterima);
        this.namaIlmiahTaksa = namaIlmiahTaksa;
        this.bagianAnatomi = bagianAnatomi;
        this.formasiGeologi = formasiGeologi;
    }

    @Override
    public double getFaktorKategori() {
        return KategoriKoleksi.FOSIL.getFaktor(); // 0.8
    }

    @Override
    public int getIntervalPemeriksaanHari() {
        return 120; // Rekomendasi 4 bulan sekali
    }

    @Override
    public String getDeskripsiKhusus() {
        StringBuilder sb = new StringBuilder();
        if (namaIlmiahTaksa != null && !namaIlmiahTaksa.isEmpty()) sb.append("Taksa: ").append(namaIlmiahTaksa).append(" | ");
        if (bagianAnatomi != null && !bagianAnatomi.isEmpty()) sb.append("Anatomi: ").append(bagianAnatomi).append(" | ");
        if (formasiGeologi != null && !formasiGeologi.isEmpty()) sb.append("Formasi: ").append(formasiGeologi);
        return sb.toString();
    }

    public String getNamaIlmiahTaksa() {
        return namaIlmiahTaksa != null ? namaIlmiahTaksa : "";
    }

    public void setNamaIlmiahTaksa(String namaIlmiahTaksa) {
        this.namaIlmiahTaksa = namaIlmiahTaksa != null ? namaIlmiahTaksa.trim() : "";
    }

    public String getBagianAnatomi() {
        return bagianAnatomi != null ? bagianAnatomi : "";
    }

    public void setBagianAnatomi(String bagianAnatomi) {
        this.bagianAnatomi = bagianAnatomi != null ? bagianAnatomi.trim() : "";
    }

    public String getFormasiGeologi() {
        return formasiGeologi != null ? formasiGeologi : "";
    }

    public void setFormasiGeologi(String formasiGeologi) {
        this.formasiGeologi = formasiGeologi != null ? formasiGeologi.trim() : "";
    }
}
