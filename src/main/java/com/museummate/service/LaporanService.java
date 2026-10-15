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

        // Header Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        Paragraph title = new Paragraph("MUSEUMMATE — LAPORAN INVENTARIS KOLEKSI MUSEUM", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph sub = new Paragraph("Dicetak pada: " + DateUtil.formatIndonesian(LocalDate.now()) + " | Total Data: " + daftarKoleksi.size() + " koleksi", subtitleFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(15);
        document.add(sub);

        // Tabel Data Koleksi
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3.5f, 1.8f, 2.0f, 2.0f, 1.8f, 2.0f});

        String[] headers = {"Kode", "Nama Koleksi", "Kategori", "Asal Daerah", "Bahan", "Kondisi", "Lokasi Simpan"};
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
        document.close();
    }

    public static void exportLaporanPrioritasPdf(List<PrioritasItem> daftarPrioritas, File targetFile) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(targetFile));

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(146, 64, 14)); // Bronze
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        Paragraph title = new Paragraph("MUSEUMMATE — DAFTAR PRIORITAS TINDAK LANJUT KONSERVASI", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph sub = new Paragraph("Algoritma Prioritas Otomatis PRD 6.4 | Dicetak pada: " + DateUtil.formatIndonesian(LocalDate.now()), subtitleFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(15);
        document.add(sub);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 1.8f, 4.0f, 1.8f, 1.8f, 2.2f, 1.8f});

        String[] headers = {"No", "Kode", "Nama Koleksi", "Kategori", "Kondisi", "Hari Terlewat", "Skor Prioritas"};
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
