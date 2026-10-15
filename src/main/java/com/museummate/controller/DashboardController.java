package com.museummate.controller;

import com.museummate.model.KategoriKoleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.PrioritasItem;
import com.museummate.service.DashboardService;
import com.museummate.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk Dashboard operasional (PRD F7).
 */
public class DashboardController {

    @FXML private Label lblTotalKoleksi;
    @FXML private Label lblPrioritasKritis;
    @FXML private Label lblJadwalTerlambat;
    @FXML private Label lblKondisiBaik;

    @FXML private Label lblNaskahCount;
    @FXML private Label lblLukisanCount;
    @FXML private Label lblArtefakCount;
    @FXML private Label lblFosilCount;

    @FXML private Label lblDetilBaik;
    @FXML private Label lblDetilCukup;
    @FXML private Label lblDetilRingan;
    @FXML private Label lblDetilBerat;

    @FXML private TableView<PrioritasItem> tableTopPrioritas;
    @FXML private TableColumn<PrioritasItem, String> colKode;
    @FXML private TableColumn<PrioritasItem, String> colNama;
    @FXML private TableColumn<PrioritasItem, String> colKategori;
    @FXML private TableColumn<PrioritasItem, String> colKondisi;
    @FXML private TableColumn<PrioritasItem, String> colHari;
    @FXML private TableColumn<PrioritasItem, String> colSkor;
    @FXML private TableColumn<PrioritasItem, String> colUrgensi;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        setupTableColumns();
        refreshDashboard();
    }

    private void setupTableColumns() {
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeRegistrasi()));
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colKategori.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori().getLabel()));
        colKondisi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKondisi().getLabel()));
        colHari.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHariTerlewat() + " hari"));
        colSkor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSkorFormatted()));
        colUrgensi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTingkatUrgensi()));

        // Styling badge urgensi
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
                    getStyleClass().removeAll("badge-kritis", "badge-tinggi", "badge-sedang", "badge-rendah");
                    switch (item.toLowerCase()) {
                        case "kritis" -> setStyle("-fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        case "tinggi" -> setStyle("-fx-text-fill: #9a3412; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        case "sedang" -> setStyle("-fx-text-fill: #1e40af; -fx-alignment: CENTER;");
                        default -> setStyle("-fx-text-fill: #166534; -fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    @FXML
    public void refreshDashboard() {
        try {
            DashboardService.DashboardStats stats = dashboardService.getStats();

            lblTotalKoleksi.setText(String.valueOf(stats.totalKoleksi()));
            lblPrioritasKritis.setText(String.valueOf(stats.jumlahPrioritasMendesak()));
            lblJadwalTerlambat.setText(String.valueOf(stats.jumlahJadwalTerlambat()));

            int baik = stats.perKondisi().getOrDefault(KondisiKoleksi.BAIK, 0);
            lblKondisiBaik.setText(String.valueOf(baik));

            // Kategori
            lblNaskahCount.setText(stats.perKategori().getOrDefault(KategoriKoleksi.NASKAH, 0) + " item");
            lblLukisanCount.setText(stats.perKategori().getOrDefault(KategoriKoleksi.LUKISAN, 0) + " item");
            lblArtefakCount.setText(stats.perKategori().getOrDefault(KategoriKoleksi.ARTEFAK, 0) + " item");
            lblFosilCount.setText(stats.perKategori().getOrDefault(KategoriKoleksi.FOSIL, 0) + " item");

            // Kondisi
            lblDetilBaik.setText(String.valueOf(baik));
            lblDetilCukup.setText(String.valueOf(stats.perKondisi().getOrDefault(KondisiKoleksi.CUKUP, 0)));
            lblDetilRingan.setText(String.valueOf(stats.perKondisi().getOrDefault(KondisiKoleksi.RUSAK_RINGAN, 0)));
            lblDetilBerat.setText(String.valueOf(stats.perKondisi().getOrDefault(KondisiKoleksi.RUSAK_BERAT, 0)));

            // Top Priority (ambil 5 teratas)
            List<PrioritasItem> topList = stats.topPrioritas();
            if (topList.size() > 5) {
                topList = topList.subList(0, 5);
            }
            tableTopPrioritas.setItems(FXCollections.observableArrayList(topList));

        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Data", "Gagal memuat data statistik dashboard: " + e.getMessage());
        }
    }
}
