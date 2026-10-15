package com.museummate.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.museummate.model.Koleksi;
import com.museummate.model.PrioritasItem;
import com.museummate.util.DateUtil;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Layanan pencetakan dan ekspor laporan berkas PDF resmi museum (PRD F7 & 8.1)
 * menggunakan library OpenPDF.
 */
public class LaporanService {

    public static void exportLaporanKoleksiPdf(List<Koleksi> daftarKoleksi, File targetFile) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(targetFile));

        document.open();

        // Kop Surat Resmi Pemkot Surabaya & Museum Surabaya (Gedung Siola)
        Font kopHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Font kopSubFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(30, 41, 59));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font ttdFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        Paragraph kop1 = new Paragraph("PEMERINTAH KOTA SURABAYA", kopHeaderFont);
        kop1.setAlignment(Element.ALIGN_CENTER);
        document.add(kop1);

        Paragraph kop2 = new Paragraph("DINAS KEBUDAYAAN, KEPEMUDAAN DAN OLAHRAGA SERTA PARIWISATA", kopHeaderFont);
        kop2.setAlignment(Element.ALIGN_CENTER);
        document.add(kop2);

        Paragraph kop3 = new Paragraph("UPTD PENGELOLAAN MUSEUM DAN GEDUNG SENI BUDAYA — MUSEUM SURABAYA (GEDUNG SIOLA)", kopSubFont);
        kop3.setAlignment(Element.ALIGN_CENTER);
        document.add(kop3);

        Paragraph kop4 = new Paragraph("Jl. Tunjungan No. 1–3, Genteng, Surabaya — Telp. (031) 5312144 | Laman: surabaya.go.id", kopSubFont);
        kop4.setAlignment(Element.ALIGN_CENTER);
        kop4.setSpacingAfter(8);
        document.add(kop4);

        // Garis Pembatas Kop
        Paragraph line = new Paragraph("________________________________________________________________________________________________________________________", kopSubFont);
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingAfter(12);
        document.add(line);

        Paragraph title = new Paragraph("LAPORAN INVENTARIS RESMI KOLEKSI CAGAR BUDAYA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph sub = new Paragraph("Basis Data Master Siola | Dicetak pada: " + DateUtil.formatIndonesian(LocalDate.now()) + " | Total: " + daftarKoleksi.size() + " Koleksi Terdaftar", subtitleFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(14);
        document.add(sub);

        // Tabel Data Koleksi
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3.8f, 1.6f, 2.0f, 2.0f, 1.7f, 2.2f});

        String[] headers = {"Kode", "Nama Koleksi", "Kategori", "Asal Daerah", "Bahan / Material", "Kondisi", "Lokasi Display Siola"};
        Color thBg = new Color(30, 41, 59); // Slate-800

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
            cell.setBackgroundColor(thBg);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        boolean alt = false;
        Color altColor = new Color(248, 250, 252);

        for (Koleksi k : daftarKoleksi) {
            Color rowBg = alt ? altColor : Color.WHITE;

            addCell(table, k.getKodeRegistrasi(), tdFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, k.getNama(), tdFont, rowBg, Element.ALIGN_LEFT);
            addCell(table, k.getKategori().getLabel(), tdFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, k.getAsal(), tdFont, rowBg, Element.ALIGN_LEFT);
            addCell(table, k.getBahan(), tdFont, rowBg, Element.ALIGN_LEFT);
            addCell(table, k.getKondisi().getLabel(), tdFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, k.getLokasiPenyimpanan(), tdFont, rowBg, Element.ALIGN_LEFT);

            alt = !alt;
        }

        document.add(table);

        // Tanda Tangan Pengesahan Kuratorial
        Paragraph ttd = new Paragraph("\nSurabaya, " + DateUtil.formatIndonesian(LocalDate.now()) + "\nKonservator / Kurator Museum Surabaya (Gedung Siola)\n\n\n\n( .............................................................. )\nNIP. ...........................................................", ttdFont);
        ttd.setAlignment(Element.ALIGN_RIGHT);
        ttd.setIndentationRight(40);
        document.add(ttd);

        document.close();
    }

    public static void exportLaporanPrioritasPdf(List<PrioritasItem> daftarPrioritas, File targetFile) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(targetFile));

        document.open();

        Font kopHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Font kopSubFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(146, 64, 14)); // Bronze
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font ttdFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        Paragraph kop1 = new Paragraph("PEMERINTAH KOTA SURABAYA", kopHeaderFont);
        kop1.setAlignment(Element.ALIGN_CENTER);
        document.add(kop1);

        Paragraph kop2 = new Paragraph("DINAS KEBUDAYAAN, KEPEMUDAAN DAN OLAHRAGA SERTA PARIWISATA", kopHeaderFont);
        kop2.setAlignment(Element.ALIGN_CENTER);
        document.add(kop2);

        Paragraph kop3 = new Paragraph("UPTD PENGELOLAAN MUSEUM DAN GEDUNG SENI BUDAYA — MUSEUM SURABAYA (GEDUNG SIOLA)", kopSubFont);
        kop3.setAlignment(Element.ALIGN_CENTER);
        document.add(kop3);

        Paragraph kop4 = new Paragraph("Jl. Tunjungan No. 1–3, Genteng, Surabaya — Telp. (031) 5312144 | Laman: surabaya.go.id", kopSubFont);
        kop4.setAlignment(Element.ALIGN_CENTER);
        kop4.setSpacingAfter(8);
        document.add(kop4);

        // Garis Pembatas Kop
        Paragraph line = new Paragraph("________________________________________________________________________________________________________________________", kopSubFont);
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingAfter(12);
        document.add(line);

        Paragraph title = new Paragraph("LAPORAN DAFTAR PRIORITAS TINDAK LANJUT KONSERVASI & PERAWATAN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph sub = new Paragraph("Algoritma Prioritas Otomatis PRD 6.4 | Dicetak pada: " + DateUtil.formatIndonesian(LocalDate.now()) + " | Total Antrean: " + daftarPrioritas.size() + " Koleksi", subtitleFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(14);
        document.add(sub);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 1.8f, 4.2f, 1.8f, 1.8f, 2.0f, 2.2f});

        String[] headers = {"No", "Kode", "Nama Koleksi", "Kategori", "Kondisi", "Hari Terlewat", "Skor Prioritas (Urgensi)"};
        Color thBg = new Color(146, 64, 14);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
            cell.setBackgroundColor(thBg);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        int rank = 1;
        boolean alt = false;
        Color altColor = new Color(254, 252, 246);

        for (PrioritasItem item : daftarPrioritas) {
            Color rowBg = alt ? altColor : Color.WHITE;

            addCell(table, String.valueOf(rank++), tdFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, item.getKodeRegistrasi(), tdFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, item.getNama(), tdFont, rowBg, Element.ALIGN_LEFT);
            addCell(table, item.getKategori().getLabel(), tdFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, item.getKondisi().getLabel(), tdFont, rowBg, Element.ALIGN_CENTER);
            addCell(table, item.getHariTerlewat() + " hari", tdFont, rowBg, Element.ALIGN_RIGHT);
            addCell(table, item.getSkorFormatted() + " (" + item.getTingkatUrgensi() + ")", tdFont, rowBg, Element.ALIGN_CENTER);

            alt = !alt;
        }

        document.add(table);

        Paragraph ttd = new Paragraph("\nSurabaya, " + DateUtil.formatIndonesian(LocalDate.now()) + "\nKonservator / Kurator Museum Surabaya (Gedung Siola)\n\n\n\n( .............................................................. )\nNIP. ...........................................................", ttdFont);
        ttd.setAlignment(Element.ALIGN_RIGHT);
        ttd.setIndentationRight(40);
        document.add(ttd);

        document.close();
    }

    private static void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }
}
