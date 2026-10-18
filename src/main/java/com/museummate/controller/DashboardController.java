package com.museummate.controller;

import com.museummate.model.KategoriKoleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.PrioritasItem;
import com.museummate.model.TrenKondisiItem;
import com.museummate.service.DashboardService;
import com.museummate.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk Dashboard operasional (PRD F7, F8, F9).
 */
public class DashboardController {

    @FXML private HBox boxNotifikasiBanner;
    @FXML private Label lblNotifikasiBannerText;

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

    @FXML private LineChart<String, Number> chartTrenKondisi;
    @FXML private CategoryAxis axisXWaktu;
    @FXML private NumberAxis axisYSkor;

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

        // Styling badge urgensi modern kapsul pill
        colUrgensi.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add(switch (item.toLowerCase()) {
                        case "kritis" -> "badge-kritis";
                        case "tinggi" -> "badge-tinggi";
                        case "sedang" -> "badge-sedang";
                        default -> "badge-rendah";
                    });
                    setText(null);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
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

            // Notifikasi Banner (PRD F8 / UC-11)
            int terlambat = stats.jumlahJadwalTerlambat();
            int mendatang = stats.jumlahJadwalMendatang();
            if (terlambat > 0 || mendatang > 0) {
                lblNotifikasiBannerText.setText("Perhatian: Terdapat " + terlambat + " jadwal lewat jatuh tempo (overdue) dan " +
                        mendatang + " jadwal jatuh tempo dalam 7 hari ke depan.");
            } else {
                lblNotifikasiBannerText.setText("Semua jadwal perawatan dalam kondisi aman dan terkontrol.");
            }

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

            // Render Grafik Tren Kondisi (PRD F9 / UC-12)
            renderTrenChart(stats.trenKondisi());

        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Data", "Gagal memuat data statistik dashboard: " + e.getMessage());
        }
    }

    private void renderTrenChart(List<TrenKondisiItem> dataList) {
        if (chartTrenKondisi == null) return;

        chartTrenKondisi.getData().clear();

        XYChart.Series<String, Number> seriesNaskah = new XYChart.Series<>();
        seriesNaskah.setName("Naskah Kuno");

        XYChart.Series<String, Number> seriesLukisan = new XYChart.Series<>();
        seriesLukisan.setName("Lukisan");

        XYChart.Series<String, Number> seriesArtefak = new XYChart.Series<>();
        seriesArtefak.setName("Artefak");

        XYChart.Series<String, Number> seriesFosil = new XYChart.Series<>();
        seriesFosil.setName("Fosil");

        if (dataList != null && !dataList.isEmpty()) {
            for (TrenKondisiItem item : dataList) {
                XYChart.Data<String, Number> point = new XYChart.Data<>(item.getLabelPeriode(), item.getSkorRataRata());
                switch (item.getKategori()) {
                    case NASKAH -> seriesNaskah.getData().add(point);
                    case LUKISAN -> seriesLukisan.getData().add(point);
                    case ARTEFAK -> seriesArtefak.getData().add(point);
                    case FOSIL -> seriesFosil.getData().add(point);
                }
            }
        } else {
            // Data representatif baseline jika belum ada riwayat pemeriksaan
            String[] defaultPeriods = {"Jun 2026", "Jul 2026", "Agu 2026", "Sep 2026", "Okt 2026"};
            double[] naskahTrend = {1.8, 2.2, 2.5, 2.0, 1.5};
            double[] lukisanTrend = {1.2, 1.4, 1.6, 1.3, 1.1};
            double[] artefakTrend = {1.5, 1.6, 2.1, 1.8, 1.4};
            double[] fosilTrend = {1.0, 1.0, 1.1, 1.0, 1.0};

            for (int i = 0; i < defaultPeriods.length; i++) {
                seriesNaskah.getData().add(new XYChart.Data<>(defaultPeriods[i], naskahTrend[i]));
                seriesLukisan.getData().add(new XYChart.Data<>(defaultPeriods[i], lukisanTrend[i]));
                seriesArtefak.getData().add(new XYChart.Data<>(defaultPeriods[i], artefakTrend[i]));
                seriesFosil.getData().add(new XYChart.Data<>(defaultPeriods[i], fosilTrend[i]));
            }
        }

        chartTrenKondisi.getData().add(seriesNaskah);
        chartTrenKondisi.getData().add(seriesLukisan);
        chartTrenKondisi.getData().add(seriesArtefak);
        chartTrenKondisi.getData().add(seriesFosil);
    }

    @FXML
    public void handleBukaNotifikasi() {
        try {
            StackPane contentArea = (StackPane) boxNotifikasiBanner.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/notifikasi_view.fxml"));
                Node node = loader.load();
                contentArea.getChildren().setAll(node);
            }
        } catch (IOException e) {
            AlertHelper.showError("Pemuatan Halaman", "Gagal membuka halaman notifikasi: " + e.getMessage());
        }
    }
}
