package com.museummate.controller;

import com.museummate.model.PrioritasItem;
import com.museummate.service.LaporanService;
import com.museummate.service.PrioritasService;
import com.museummate.util.AlertHelper;
import com.museummate.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller untuk Fitur Utama Prioritas Perawatan Otomatis (PRD F6 & 6.4).
 */
public class PrioritasController {

    @FXML private DatePicker dpTanggalAcuan;
    @FXML private TableView<PrioritasItem> tablePrioritas;

    @FXML private TableColumn<PrioritasItem, String> colNo;
    @FXML private TableColumn<PrioritasItem, String> colKode;
    @FXML private TableColumn<PrioritasItem, String> colNama;
    @FXML private TableColumn<PrioritasItem, String> colKategori;
    @FXML private TableColumn<PrioritasItem, String> colKondisi;
    @FXML private TableColumn<PrioritasItem, String> colAcuan;
    @FXML private TableColumn<PrioritasItem, String> colHari;
    @FXML private TableColumn<PrioritasItem, String> colSkor;
    @FXML private TableColumn<PrioritasItem, String> colUrgensi;

    @FXML private Label lblSummary;

    private final PrioritasService prioritasService = new PrioritasService();
    private final ObservableList<PrioritasItem> prioritasList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dpTanggalAcuan.setValue(LocalDate.now());
        setupTableColumns();
        loadDataPrioritas();
    }

    private void setupTableColumns() {
        colNo.setCellValueFactory(cellData -> {
            int index = tablePrioritas.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });

        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeRegistrasi()));
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colKategori.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getKategori().getLabel() + " (" + c.getValue().getKategori().getFaktor() + ")"));
        colKondisi.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getKondisi().getLabel() + " (B: " + c.getValue().getKondisi().getBobot() + ")"));
        colAcuan.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().isBelumPernahDiperiksa() ? "[Diterima] " : "[Periksa] ") +
                DateUtil.formatShort(c.getValue().getTanggalAcuan())));
        colHari.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHariTerlewat() + " hari"));
        colSkor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSkorFormatted()));
        colUrgensi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTingkatUrgensi()));

        // Warna badge urgensi
        colUrgensi.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "kritis" -> setStyle("-fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        case "tinggi" -> setStyle("-fx-text-fill: #9a3412; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        case "sedang" -> setStyle("-fx-text-fill: #1e40af; -fx-alignment: CENTER;");
                        default -> setStyle("-fx-text-fill: #166534; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        tablePrioritas.setItems(prioritasList);
    }

    @FXML
    public void handleTanggalAcuanChange() {
        loadDataPrioritas();
    }

    @FXML
    public void loadDataPrioritas() {
        try {
            LocalDate acuan = dpTanggalAcuan.getValue();
            if (acuan == null) {
                acuan = LocalDate.now();
                dpTanggalAcuan.setValue(acuan);
            }

            List<PrioritasItem> list = prioritasService.getDaftarPrioritas(acuan);
            prioritasList.setAll(list);
            lblSummary.setText("Total: " + list.size() + " koleksi dihitung dan diurutkan dari skor tertinggi.");
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Data", "Gagal menghitung daftar prioritas: " + e.getMessage());
        }
    }

    @FXML
    public void handleCatatPemeriksaan() {
        PrioritasItem selected = tablePrioritas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih koleksi pada tabel yang ingin dicatat pemeriksaannya.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/catat_pemeriksaan_dialog.fxml"));
            Parent root = loader.load();

            CatatPemeriksaanController controller = loader.getController();
            controller.setKoleksiTarget(selected.getKoleksi());
            controller.setOnSuccessCallback(this::loadDataPrioritas);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Catat Pemeriksaan Fisik");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tablePrioritas.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Dialog", "Gagal membuka form pemeriksaan: " + e.getMessage());
        }
    }

    @FXML
    public void handleBuatJadwal() {
        PrioritasItem selected = tablePrioritas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih koleksi pada tabel yang ingin dibuatkan jadwal perawatannya.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/tambah_jadwal_dialog.fxml"));
            Parent root = loader.load();

            TambahJadwalController controller = loader.getController();
            controller.setKoleksiTarget(selected.getKoleksi());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Susun Jadwal Perawatan");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tablePrioritas.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Dialog", "Gagal membuka form jadwal: " + e.getMessage());
        }
    }

    @FXML
    public void handleExportPdf() {
        if (prioritasList.isEmpty()) {
            AlertHelper.showWarning("Peringatan", "Tidak ada data prioritas untuk diekspor.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Daftar Prioritas PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dokumen PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("Daftar_Prioritas_Konservasi_MuseumMate.pdf");

        File file = fileChooser.showSaveDialog(tablePrioritas.getScene().getWindow());
        if (file != null) {
            try {
                LaporanService.exportLaporanPrioritasPdf(prioritasList, file);
                AlertHelper.showInfo("Ekspor Berhasil", "Laporan PDF daftar prioritas berhasil disimpan:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                AlertHelper.showError("Ekspor Gagal", "Gagal menyimpan berkas PDF: " + e.getMessage());
            }
        }
    }
}
