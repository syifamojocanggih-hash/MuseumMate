package com.museummate.controller;

import com.museummate.model.JadwalPerawatan;
import com.museummate.model.StatusJadwal;
import com.museummate.service.JadwalService;
import com.museummate.service.LogAktivitasService;
import com.museummate.util.AlertHelper;
import com.museummate.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller untuk Peringatan & Notifikasi Jadwal Perawatan (PRD F8 / UC-11).
 * Dapat diakses oleh Admin maupun Konservator.
 */
public class NotifikasiController {

    @FXML private Label lblTotalNotifikasi;
    @FXML private Label lblTerlambatCount;
    @FXML private Label lblMendatangCount;

    @FXML private TableView<JadwalPerawatan> tableNotifikasi;
    @FXML private TableColumn<JadwalPerawatan, String> colStatusNotifikasi;
    @FXML private TableColumn<JadwalPerawatan, String> colTanggalRencana;
    @FXML private TableColumn<JadwalPerawatan, String> colKodeKoleksi;
    @FXML private TableColumn<JadwalPerawatan, String> colNamaKoleksi;
    @FXML private TableColumn<JadwalPerawatan, String> colKegiatan;
    @FXML private TableColumn<JadwalPerawatan, String> colKeterangan;

    @FXML private Button btnTandaiSelesai;
    @FXML private Label lblStatusData;

    private final JadwalService jadwalService = new JadwalService();
    private final ObservableList<JadwalPerawatan> notifList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadNotifikasiData();
    }

    private void setupTableColumns() {
        colTanggalRencana.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTanggalRencana() != null ? DateUtil.formatIndonesian(c.getValue().getTanggalRencana()) : "-"));
        colKodeKoleksi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeKoleksi()));
        colNamaKoleksi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaKoleksi()));
        colKegiatan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKegiatan()));
        colKeterangan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKeterangan()));

        // Status notifikasi dengan badge visual: OVERDUE vs UPCOMING
        colStatusNotifikasi.setCellValueFactory(c -> {
            LocalDate tgl = c.getValue().getTanggalRencana();
            if (tgl == null) return new SimpleStringProperty("-");

            LocalDate now = LocalDate.now();
            long selisih = ChronoUnit.DAYS.between(now, tgl);
            if (selisih < 0) {
                return new SimpleStringProperty("TERLAMBAT (" + Math.abs(selisih) + " hari)");
            } else if (selisih == 0) {
                return new SimpleStringProperty("JATUH TEMPO HARI INI");
            } else {
                return new SimpleStringProperty("SEGERA (H-" + selisih + ")");
            }
        });

        colStatusNotifikasi.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.startsWith("TERLAMBAT") || item.contains("HARI INI")) {
                        badge.getStyleClass().add("badge-terlambat");
                    } else {
                        badge.getStyleClass().add("badge-upcoming");
                    }
                    setText(null);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    @FXML
    public void loadNotifikasiData() {
        try {
            List<JadwalPerawatan> overdue = jadwalService.getJadwalTerlambat();
            List<JadwalPerawatan> upcoming = jadwalService.getJadwalMendatang(7);

            int totalTerlambat = overdue.size();
            int totalMendatang = upcoming.size();
            int total = totalTerlambat + totalMendatang;

            lblTerlambatCount.setText(String.valueOf(totalTerlambat));
            lblMendatangCount.setText(String.valueOf(totalMendatang));
            lblTotalNotifikasi.setText(String.valueOf(total));

            List<JadwalPerawatan> combined = new ArrayList<>();
            combined.addAll(overdue);
            combined.addAll(upcoming);

            notifList.setAll(combined);
            tableNotifikasi.setItems(notifList);

            if (lblStatusData != null) {
                lblStatusData.setText("Menampilkan " + total + " jadwal yang membutuhkan perhatian segera.");
            }
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Basis Data", "Gagal memuat notifikasi jadwal: " + e.getMessage());
        }
    }

    @FXML
    public void handleTandaiSelesai() {
        JadwalPerawatan selected = tableNotifikasi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih salah satu jadwal yang ingin ditandai selesai.");
            return;
        }

        try {
            jadwalService.ubahStatus(selected.getIdJadwal(), StatusJadwal.SELESAI);
            LogAktivitasService.catat("UBAH", "jadwal_perawatan",
                    "Menandai SELESAI jadwal perawatan lewat menu notifikasi ID #" + selected.getIdJadwal() + " (" + selected.getNamaKoleksi() + ")");
            AlertHelper.showInfo("Sukses", "Jadwal perawatan berhasil diselesaikan dan dihapus dari daftar notifikasi.");
            loadNotifikasiData();
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memperbarui status: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        loadNotifikasiData();
    }
}
