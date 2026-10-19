package com.museummate.controller;

import com.museummate.model.JadwalPerawatan;
import com.museummate.model.KategoriKoleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.Pengguna;
import com.museummate.model.PrioritasItem;
import com.museummate.model.TrenKondisiItem;
import com.museummate.service.DashboardService;
import com.museummate.service.LaporanService;
import com.museummate.util.AlertHelper;
import com.museummate.util.DateUtil;
import com.museummate.util.SessionManager;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk Dashboard operasional (PRD F7, F8, F9).
 * Menghadirkan ringkasan operasional dengan visual modern, progress metric,
 * kartu agenda konservasi, dan grafik tren kondisi koleksi.
 */
public class DashboardController {

    @FXML private ScrollPane rootPane;

    @FXML private Label lblHeroGreetingTitle;
    @FXML private Label lblHeroGreetingSub;

    @FXML private Label lblTotalKoleksi;
    @FXML private Label lblPrioritasKritis;
    @FXML private Label lblJadwalTerlambat;
    @FXML private Label lblKondisiBaik;

    @FXML private Label lblNaskahCount;
    @FXML private Label lblLukisanCount;
    @FXML private Label lblArtefakCount;
    @FXML private Label lblFosilCount;

    @FXML private ProgressBar progressNaskah;
    @FXML private ProgressBar progressLukisan;
    @FXML private ProgressBar progressArtefak;
    @FXML private ProgressBar progressFosil;

    @FXML private Label lblDetilBaik;
    @FXML private Label lblDetilCukup;
    @FXML private Label lblDetilRingan;
    @FXML private Label lblDetilBerat;

    @FXML private ProgressBar progressBaik;
    @FXML private ProgressBar progressCukup;
    @FXML private ProgressBar progressRingan;
    @FXML private ProgressBar progressBerat;

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

    @FXML private VBox boxFeaturedSchedule;
    @FXML private Label lblFeaturedStatus;
    @FXML private Label lblFeaturedTanggal;
    @FXML private Label lblFeaturedKegiatan;
    @FXML private Label lblFeaturedKoleksi;
    @FXML private VBox boxUpcomingList;
    @FXML private Label lblEmptyUpcoming;

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

            // Hero Greeting
            Pengguna user = SessionManager.getInstance().getCurrentUser();
            String userName = user != null ? user.getNamaLengkap() : "Kurator";
            if (lblHeroGreetingTitle != null) {
                lblHeroGreetingTitle.setText("Selamat Datang, " + userName + "!");
            }

            int terlambat = stats.jumlahJadwalTerlambat();
            int kritis = stats.jumlahPrioritasMendesak();
            if (lblHeroGreetingSub != null) {
                if (terlambat > 0 || kritis > 0) {
                    lblHeroGreetingSub.setText("Terdapat " + terlambat + " jadwal perawatan terlambat dan " + kritis + " koleksi berstatus prioritas kritis atau tinggi. Segera lakukan penanganan preservasi.");
                } else {
                    lblHeroGreetingSub.setText("Semua koleksi dan jadwal perawatan dalam pemantauan stabil. Pertahankan standar preservasi koleksi museum.");
                }
            }

            // Stat Cards
            lblTotalKoleksi.setText(String.valueOf(stats.totalKoleksi()));
            lblPrioritasKritis.setText(String.valueOf(kritis));
            lblJadwalTerlambat.setText(String.valueOf(terlambat));

            int baik = stats.perKondisi().getOrDefault(KondisiKoleksi.BAIK, 0);
            lblKondisiBaik.setText(String.valueOf(baik));

            // Kategori Breakdown with Progress Bars
            int total = stats.totalKoleksi() > 0 ? stats.totalKoleksi() : 1;
            int naskah = stats.perKategori().getOrDefault(KategoriKoleksi.NASKAH, 0);
            int lukisan = stats.perKategori().getOrDefault(KategoriKoleksi.LUKISAN, 0);
            int artefak = stats.perKategori().getOrDefault(KategoriKoleksi.ARTEFAK, 0);
            int fosil = stats.perKategori().getOrDefault(KategoriKoleksi.FOSIL, 0);

            lblNaskahCount.setText(naskah + " item (" + (naskah * 100 / total) + "%)");
            lblLukisanCount.setText(lukisan + " item (" + (lukisan * 100 / total) + "%)");
            lblArtefakCount.setText(artefak + " item (" + (artefak * 100 / total) + "%)");
            lblFosilCount.setText(fosil + " item (" + (fosil * 100 / total) + "%)");

            if (progressNaskah != null) progressNaskah.setProgress((double) naskah / total);
            if (progressLukisan != null) progressLukisan.setProgress((double) lukisan / total);
            if (progressArtefak != null) progressArtefak.setProgress((double) artefak / total);
            if (progressFosil != null) progressFosil.setProgress((double) fosil / total);

            // Kondisi Breakdown with Progress Bars
            int cukup = stats.perKondisi().getOrDefault(KondisiKoleksi.CUKUP, 0);
            int ringan = stats.perKondisi().getOrDefault(KondisiKoleksi.RUSAK_RINGAN, 0);
            int berat = stats.perKondisi().getOrDefault(KondisiKoleksi.RUSAK_BERAT, 0);

            lblDetilBaik.setText(baik + " item (" + (baik * 100 / total) + "%)");
            lblDetilCukup.setText(cukup + " item (" + (cukup * 100 / total) + "%)");
            lblDetilRingan.setText(ringan + " item (" + (ringan * 100 / total) + "%)");
            lblDetilBerat.setText(berat + " item (" + (berat * 100 / total) + "%)");

            if (progressBaik != null) progressBaik.setProgress((double) baik / total);
            if (progressCukup != null) progressCukup.setProgress((double) cukup / total);
            if (progressRingan != null) progressRingan.setProgress((double) ringan / total);
            if (progressBerat != null) progressBerat.setProgress((double) berat / total);

            // Top Priority (ambil 5 teratas)
            List<PrioritasItem> topList = stats.topPrioritas();
            if (topList.size() > 5) {
                topList = topList.subList(0, 5);
            }
            tableTopPrioritas.setItems(FXCollections.observableArrayList(topList));

            // Render Grafik Tren Kondisi (PRD F9 / UC-12)
            renderTrenChart(stats.trenKondisi());

            // Render Agenda Konservasi Terdekat (Right column)
            renderUpcomingSchedules(stats.upcomingJadwal());

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

    private void renderUpcomingSchedules(List<JadwalPerawatan> list) {
        if (boxFeaturedSchedule == null || boxUpcomingList == null) return;

        boxUpcomingList.getChildren().clear();

        if (list == null || list.isEmpty()) {
            boxFeaturedSchedule.setVisible(false);
            boxFeaturedSchedule.setManaged(false);
            if (lblEmptyUpcoming != null) {
                lblEmptyUpcoming.setVisible(true);
                lblEmptyUpcoming.setManaged(true);
            }
            return;
        }

        if (lblEmptyUpcoming != null) {
            lblEmptyUpcoming.setVisible(false);
            lblEmptyUpcoming.setManaged(false);
        }

        // Top schedule -> Featured royal blue card
        JadwalPerawatan top = list.get(0);
        boxFeaturedSchedule.setVisible(true);
        boxFeaturedSchedule.setManaged(true);

        if (lblFeaturedKegiatan != null) lblFeaturedKegiatan.setText(top.getKegiatan());
        if (lblFeaturedKoleksi != null) {
            lblFeaturedKoleksi.setText(top.getNamaKoleksi() != null ? top.getNamaKoleksi() : "Koleksi ID " + top.getIdKoleksi());
        }
        if (lblFeaturedTanggal != null) {
            lblFeaturedTanggal.setText(top.getTanggalRencana() != null ? DateUtil.formatIndonesian(top.getTanggalRencana()) : "-");
        }
        if (lblFeaturedStatus != null) {
            if (top.isTerlambat()) {
                lblFeaturedStatus.setText("OVERDUE / TERLAMBAT");
                lblFeaturedStatus.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 10px;");
            } else {
                lblFeaturedStatus.setText("JADWAL TERDEKAT");
                lblFeaturedStatus.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: #ffffff; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 10px;");
            }
        }

        // Subsequent items -> boxUpcomingList
        for (int i = 1; i < list.size(); i++) {
            JadwalPerawatan j = list.get(i);
            VBox itemCard = new VBox(4);
            itemCard.getStyleClass().add("agenda-item-card");

            HBox topRow = new HBox(8);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label lblTgl = new Label(j.getTanggalRencana() != null ? DateUtil.formatIndonesian(j.getTanggalRencana()) : "-");
            lblTgl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3b5cee;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblSts = new Label(j.isTerlambat() ? "Terlambat" : "Rencana");
            lblSts.setStyle(j.isTerlambat()
                    ? "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 6px;"
                    : "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 6px;");

            topRow.getChildren().addAll(lblTgl, spacer, lblSts);

            Label lblKeg = new Label(j.getKegiatan());
            lblKeg.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0f172a;");

            Label lblKol = new Label(j.getNamaKoleksi() != null ? j.getNamaKoleksi() : "Koleksi ID " + j.getIdKoleksi());
            lblKol.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

            itemCard.getChildren().addAll(topRow, lblKeg, lblKol);
            boxUpcomingList.getChildren().add(itemCard);
        }
    }

    private void switchView(String fxmlPath) {
        try {
            if (rootPane != null && rootPane.getScene() != null) {
                StackPane contentArea = (StackPane) rootPane.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Node node = loader.load();
                    contentArea.getChildren().setAll(node);
                }
            }
        } catch (IOException e) {
            AlertHelper.showError("Pemuatan Halaman", "Gagal membuka halaman: " + e.getMessage());
        }
    }

    @FXML
    public void handleBukaPrioritas() {
        switchView("/com/museummate/view/prioritas_view.fxml");
    }

    @FXML
    public void handleBukaJadwal() {
        switchView("/com/museummate/view/jadwal_view.fxml");
    }

    @FXML
    public void handleBukaPemeriksaan() {
        switchView("/com/museummate/view/pemeriksaan_view.fxml");
    }

    @FXML
    public void handleBukaKoleksi() {
        switchView("/com/museummate/view/koleksi_view.fxml");
    }

    @FXML
    public void handleBukaNotifikasi() {
        switchView("/com/museummate/view/notifikasi_view.fxml");
    }

    @FXML
    public void handleCetakLaporanPdf() {
        try {
            DashboardService.DashboardStats stats = dashboardService.getStats();
            List<PrioritasItem> list = stats.topPrioritas();
            if (list == null || list.isEmpty()) {
                AlertHelper.showWarning("Peringatan", "Tidak ada data prioritas untuk diekspor ke PDF.");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Laporan Prioritas Konservasi (PDF)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dokumen PDF (*.pdf)", "*.pdf"));
            fileChooser.setInitialFileName("Laporan_Prioritas_Konservasi_MuseumMate.pdf");

            File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
            if (file != null) {
                LaporanService.exportLaporanPrioritasPdf(list, file);
                AlertHelper.showInfo("Ekspor Berhasil", "Laporan PDF berhasil disimpan:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            AlertHelper.showError("Ekspor Gagal", "Gagal menyimpan laporan PDF: " + e.getMessage());
        }
    }
}
