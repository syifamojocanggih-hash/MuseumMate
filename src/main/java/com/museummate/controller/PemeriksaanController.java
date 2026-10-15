package com.museummate.controller;

import com.museummate.model.Koleksi;
import com.museummate.model.Pemeriksaan;
import com.museummate.service.KoleksiService;
import com.museummate.service.PemeriksaanService;
import com.museummate.util.AlertHelper;
import com.museummate.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk penelusuran riwayat pemeriksaan fisik (PRD F4).
 */
public class PemeriksaanController {

    @FXML private ComboBox<Koleksi> cmbPilihKoleksi;
    @FXML private Label lblTotalCatatan;

    @FXML private TableView<Pemeriksaan> tablePemeriksaan;
    @FXML private TableColumn<Pemeriksaan, String> colTanggal;
    @FXML private TableColumn<Pemeriksaan, String> colKode;
    @FXML private TableColumn<Pemeriksaan, String> colNama;
    @FXML private TableColumn<Pemeriksaan, String> colKondisiHasil;
    @FXML private TableColumn<Pemeriksaan, String> colTindakan;
    @FXML private TableColumn<Pemeriksaan, String> colCatatan;
    @FXML private TableColumn<Pemeriksaan, String> colPetugas;

    private final PemeriksaanService pemeriksaanService = new PemeriksaanService();
    private final KoleksiService koleksiService = new KoleksiService();
    private final ObservableList<Pemeriksaan> pemeriksaanList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadKoleksiComboBox();
        loadAllRecentPemeriksaan();

        cmbPilihKoleksi.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                loadPemeriksaanByKoleksi(newV.getIdKoleksi());
            }
        });
    }

    private void setupTableColumns() {
        colTanggal.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatIndonesian(c.getValue().getTanggalPemeriksaan())));
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeKoleksi()));
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaKoleksi()));
        colKondisiHasil.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKondisiHasil().getLabel()));
        colTindakan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTindakan()));
        colCatatan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCatatan()));
        colPetugas.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaPetugas()));

        tablePemeriksaan.setItems(pemeriksaanList);
    }

    private void loadKoleksiComboBox() {
        try {
            List<Koleksi> all = koleksiService.getAllKoleksi();
            cmbPilihKoleksi.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memuat daftar koleksi: " + e.getMessage());
        }
    }

    public void loadAllRecentPemeriksaan() {
        try {
            List<Pemeriksaan> list = pemeriksaanService.getRecentPemeriksaan(100);
            pemeriksaanList.setAll(list);
            lblTotalCatatan.setText("Total: " + list.size() + " catatan riwayat pemeriksaan");
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memuat riwayat pemeriksaan: " + e.getMessage());
        }
    }

    private void loadPemeriksaanByKoleksi(int idKoleksi) {
        try {
            List<Pemeriksaan> list = pemeriksaanService.getRiwayatByKoleksi(idKoleksi);
            pemeriksaanList.setAll(list);
            lblTotalCatatan.setText("Total: " + list.size() + " catatan untuk koleksi terpilih");
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memuat riwayat pemeriksaan koleksi: " + e.getMessage());
        }
    }

    @FXML
    public void handleTampilkanSemua() {
        cmbPilihKoleksi.setValue(null);
        loadAllRecentPemeriksaan();
    }

    @FXML
    public void handleBukaFormCatat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/catat_pemeriksaan_dialog.fxml"));
            Parent root = loader.load();

            CatatPemeriksaanController controller = loader.getController();
            controller.setOnSuccessCallback(() -> {
                loadAllRecentPemeriksaan();
                loadKoleksiComboBox();
            });

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Catat Pemeriksaan Fisik Baru");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tablePemeriksaan.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Dialog", "Gagal membuka form pencatatan: " + e.getMessage());
        }
    }
}
