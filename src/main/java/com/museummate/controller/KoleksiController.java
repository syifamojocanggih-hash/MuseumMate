package com.museummate.controller;

import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.Pengguna;
import com.museummate.model.PeranPengguna;
import com.museummate.service.KoleksiService;
import com.museummate.service.LaporanService;
import com.museummate.util.AlertHelper;
import com.museummate.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk antarmuka manajemen data koleksi (PRD F2 & F3).
 */
public class KoleksiController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<KategoriKoleksi> cmbKategoriFilter;
    @FXML private ComboBox<KondisiKoleksi> cmbKondisiFilter;

    @FXML private Button btnTambah;
    @FXML private Button btnUbah;
    @FXML private Button btnHapus;
    @FXML private Button btnSpesialisasi;
    @FXML private Button btnUploadFoto;

    @FXML private TableView<Koleksi> tableKoleksi;
    @FXML private TableColumn<Koleksi, String> colKode;
    @FXML private TableColumn<Koleksi, String> colNama;
    @FXML private TableColumn<Koleksi, String> colKategori;
    @FXML private TableColumn<Koleksi, String> colKondisi;
    @FXML private TableColumn<Koleksi, String> colAsal;
    @FXML private TableColumn<Koleksi, String> colBahan;
    @FXML private TableColumn<Koleksi, String> colLokasi;
    @FXML private TableColumn<Koleksi, String> colAtributKhusus;

    @FXML private Label lblStatusData;

    private final KoleksiService koleksiService = new KoleksiService();
    private final ObservableList<Koleksi> dataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupRoleAccess();
        setupFilterControls();
        setupTableColumns();
        loadDataKoleksi();
    }

    private void setupRoleAccess() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        btnTambah.setVisible(isAdmin);
        btnTambah.setManaged(isAdmin);
        btnUbah.setVisible(isAdmin);
        btnUbah.setManaged(isAdmin);
        btnHapus.setVisible(isAdmin);
        btnHapus.setManaged(isAdmin);

        // btnUploadFoto dapat diakses oleh Konservator maupun Admin
        if (btnUploadFoto != null) {
            btnUploadFoto.setVisible(true);
            btnUploadFoto.setManaged(true);
        }

        // btnSpesialisasi: fitur pintar konservator berbasis bidang keahlian
        if (btnSpesialisasi != null) {
            Pengguna user = SessionManager.getInstance().getCurrentUser();
            if (user != null && user.getPeran() == PeranPengguna.KONSERVATOR
                    && user.getSpesialisasi() != null && !user.getSpesialisasi().equalsIgnoreCase("SEMUA")) {
                btnSpesialisasi.setVisible(true);
                btnSpesialisasi.setManaged(true);
                btnSpesialisasi.setText("⭐ Khusus " + user.getSpesialisasi());
            } else {
                btnSpesialisasi.setVisible(false);
                btnSpesialisasi.setManaged(false);
            }
        }
    }

    private void setupFilterControls() {
        cmbKategoriFilter.getItems().add(null); // Option "Semua"
        cmbKategoriFilter.getItems().addAll(KategoriKoleksi.values());

        cmbKondisiFilter.getItems().add(null); // Option "Semua"
        cmbKondisiFilter.getItems().addAll(KondisiKoleksi.values());

        // Listener filter satu aksi tanpa reload manual (PRD F2)
        txtSearch.textProperty().addListener((obs, oldV, newV) -> filterData());
        cmbKategoriFilter.valueProperty().addListener((obs, oldV, newV) -> filterData());
        cmbKondisiFilter.valueProperty().addListener((obs, oldV, newV) -> filterData());
    }

    private void setupTableColumns() {
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeRegistrasi()));
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colKategori.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori().getLabel()));
        colKondisi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKondisi().getLabel()));

        // Badge kapsul modern untuk kondisi fisik
        colKondisi.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add(switch (item.toLowerCase()) {
                        case "rusak berat" -> "badge-kritis";
                        case "rusak ringan" -> "badge-tinggi";
                        case "cukup" -> "badge-sedang";
                        default -> "badge-rendah";
                    });
                    setText(null);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colAsal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAsal()));
        colBahan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBahan()));
        colLokasi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLokasiPenyimpanan()));
        colAtributKhusus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeskripsiKhusus()));

        tableKoleksi.setItems(dataList);
    }

    public void loadDataKoleksi() {
        try {
            List<Koleksi> all = koleksiService.getAllKoleksi();
            dataList.setAll(all);
            updateStatusLabel(all.size());
        } catch (SQLException e) {
            AlertHelper.showError("Gagal Memuat", "Kesalahan saat memuat data koleksi: " + e.getMessage());
        }
    }

    private void filterData() {
        String keyword = txtSearch.getText();
        KategoriKoleksi kategori = cmbKategoriFilter.getValue();
        KondisiKoleksi kondisi = cmbKondisiFilter.getValue();

        try {
            List<Koleksi> hasil = koleksiService.searchAndFilter(keyword, kategori, kondisi);
            dataList.setAll(hasil);
            updateStatusLabel(hasil.size());
        } catch (SQLException e) {
            AlertHelper.showError("Pencarian Gagal", "Terjadi kesalahan saat memfilter data: " + e.getMessage());
        }
    }

    @FXML
    public void handleResetFilter() {
        txtSearch.clear();
        cmbKategoriFilter.setValue(null);
        cmbKondisiFilter.setValue(null);
        loadDataKoleksi();
    }

    @FXML
    public void handleTambahKoleksi() {
        bukaFormDialog(null);
    }

    @FXML
    public void handleUbahKoleksi() {
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih data koleksi yang ingin diubah terlebih dahulu.");
            return;
        }
        bukaFormDialog(selected);
    }

    @FXML
    public void handleHapusKoleksi() {
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih data koleksi yang ingin dihapus terlebih dahulu.");
            return;
        }

        try {
            // PRD F2: Penghapusan koleksi yang masih memiliki riwayat pemeriksaan meminta konfirmasi tambahan
            int riwayatCount = koleksiService.cekRiwayatPemeriksaan(selected.getIdKoleksi());
            boolean konfirmasi;

            if (riwayatCount > 0) {
                konfirmasi = AlertHelper.showConfirmation(
                        "Konfirmasi Tambahan Penghapusan",
                        "PERINGATAN: Koleksi memiliki " + riwayatCount + " riwayat pemeriksaan fisik!",
                        "Menghapus koleksi '" + selected.getNama() + "' (" + selected.getKodeRegistrasi() +
                        ") akan menghapus seluruh data riwayat pemeriksaan terkait. Apakah Anda benar-benar yakin?");
            } else {
                konfirmasi = AlertHelper.showConfirmation(
                        "Konfirmasi Penghapusan",
                        "Hapus Koleksi",
                        "Apakah Anda yakin ingin menghapus '" + selected.getNama() + "' (" + selected.getKodeRegistrasi() + ")?");
            }

            if (konfirmasi) {
                boolean success = koleksiService.hapusKoleksi(selected.getIdKoleksi());
                if (success) {
                    com.museummate.service.LogAktivitasService.catat("HAPUS", "koleksi", 
                            "Menghapus koleksi: " + selected.getKodeRegistrasi() + " - " + selected.getNama());
                    AlertHelper.showInfo("Sukses", "Data koleksi berhasil dihapus.");
                    loadDataKoleksi();
                } else {
                    AlertHelper.showError("Gagal", "Koleksi tidak dapat dihapus.");
                }
            }
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Basis Data", "Gagal memproses penghapusan: " + e.getMessage());
        }
    }

    @FXML
    public void handleToggleSpesialisasi() {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user == null || user.getSpesialisasi() == null) {
            return;
        }

        try {
            KategoriKoleksi target = KategoriKoleksi.fromString(user.getSpesialisasi());
            if (cmbKategoriFilter.getValue() == target) {
                cmbKategoriFilter.setValue(null);
            } else {
                cmbKategoriFilter.setValue(target);
            }
        } catch (Exception ignored) {
            cmbKategoriFilter.setValue(null);
        }
    }

    @FXML
    public void handleUploadFoto() {
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih data koleksi yang ingin diunggah fotonya terlebih dahulu.");
            return;
        }
        unggahFotoKoleksi(selected);
    }

    private void unggahFotoKoleksi(Koleksi selected) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Koleksi: " + selected.getNama());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Gambar (*.jpg, *.jpeg, *.png, *.webp)", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );

        File file = fileChooser.showOpenDialog(tableKoleksi.getScene().getWindow());
        if (file != null) {
            try {
                File uploadDir = new File("uploads/koleksi");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String ext = "";
                int dotIdx = file.getName().lastIndexOf('.');
                if (dotIdx > 0) {
                    ext = file.getName().substring(dotIdx);
                }
                String newFileName = "koleksi_" + selected.getIdKoleksi() + "_" + System.currentTimeMillis() + ext;
                File destFile = new File(uploadDir, newFileName);
                java.nio.file.Files.copy(file.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String savedPath = "uploads/koleksi/" + newFileName;
                koleksiService.updateFotoPath(selected.getIdKoleksi(), savedPath);
                selected.setFotoPath(savedPath);

                com.museummate.service.LogAktivitasService.catat(
                        "UNGGAH_FOTO", "koleksi",
                        "Konservator/Admin mengunggah foto untuk koleksi: " + selected.getKodeRegistrasi() + " (" + selected.getNama() + ")"
                );

                AlertHelper.showInfo("Unggah Berhasil", "Foto benda koleksi berhasil diperbarui.");
                tableKoleksi.refresh();
            } catch (Exception e) {
                AlertHelper.showError("Gagal Mengunggah", "Terjadi kesalahan saat mengunggah foto: " + e.getMessage());
            }
        }
    }

    private Image muatGambar(String fotoPath) {
        if (fotoPath == null || fotoPath.trim().isEmpty()) {
            return null;
        }
        try {
            File localFile = new File(fotoPath);
            if (localFile.exists() && localFile.isFile()) {
                return new Image(localFile.toURI().toString());
            }
            String resourcePath = fotoPath.startsWith("/") ? fotoPath : "/" + fotoPath;
            var is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                return new Image(is);
            }
            String imgPath = "/com/museummate/images/" + fotoPath.replaceFirst("^images/", "").replaceFirst("^com/museummate/images/", "");
            var is2 = getClass().getResourceAsStream(imgPath);
            if (is2 != null) {
                return new Image(is2);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @FXML
    public void handleLihatDetail() {
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih salah satu koleksi untuk melihat rincian.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detail Koleksi Museum");
        dialog.setHeaderText("Rincian Benda Koleksi: " + selected.getNama() + " (" + selected.getKodeRegistrasi() + ")");
        if (tableKoleksi.getScene() != null && tableKoleksi.getScene().getWindow() != null) {
            dialog.initOwner(tableKoleksi.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            if (!tableKoleksi.getScene().getStylesheets().isEmpty()) {
                dialog.getDialogPane().getStylesheets().addAll(tableKoleksi.getScene().getStylesheets());
            }
        }

        HBox mainContent = new HBox(20);
        mainContent.setPadding(new javafx.geometry.Insets(16));
        mainContent.setAlignment(Pos.TOP_LEFT);

        VBox photoBox = new VBox(10);
        photoBox.setAlignment(Pos.CENTER);
        photoBox.setPrefWidth(260);

        StackPane imgFrame = new StackPane();
        imgFrame.setPrefSize(250, 250);
        imgFrame.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(240);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Label lblNoPhoto = new Label("Belum ada foto fisik\n(Klik tombol di bawah untuk unggah)");
        lblNoPhoto.setStyle("-fx-text-fill: #94a3b8; -fx-text-alignment: center; -fx-font-size: 11px;");

        Image img = muatGambar(selected.getFotoPath());
        if (img != null) {
            imageView.setImage(img);
            lblNoPhoto.setVisible(false);
        } else {
            lblNoPhoto.setVisible(true);
        }

        imgFrame.getChildren().addAll(lblNoPhoto, imageView);

        Button btnGantiFoto = new Button("📷 Unggah / Ganti Foto");
        btnGantiFoto.getStyleClass().add("btn-secondary");
        btnGantiFoto.setOnAction(e -> {
            unggahFotoKoleksi(selected);
            Image updatedImg = muatGambar(selected.getFotoPath());
            if (updatedImg != null) {
                imageView.setImage(updatedImg);
                lblNoPhoto.setVisible(false);
            }
        });

        photoBox.getChildren().addAll(imgFrame, btnGantiFoto);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        int row = 0;
        addDetailRow(grid, row++, "Kode Registrasi:", selected.getKodeRegistrasi());
        addDetailRow(grid, row++, "Nama Benda:", selected.getNama());
        addDetailRow(grid, row++, "Kategori:", selected.getKategori().getLabel());
        addDetailRow(grid, row++, "Kondisi Fisik:", selected.getKondisi().getLabel());
        addDetailRow(grid, row++, "Asal / Budaya:", selected.getAsal() != null ? selected.getAsal() : "-");
        addDetailRow(grid, row++, "Tahun Perkiraan:", selected.getTahunPerkiraan() != null ? selected.getTahunPerkiraan() : "-");
        addDetailRow(grid, row++, "Bahan Utama:", selected.getBahan() != null ? selected.getBahan() : "-");
        addDetailRow(grid, row++, "Sumber Perolehan:", selected.getSumberPerolehan() != null ? selected.getSumberPerolehan() : "-");
        addDetailRow(grid, row++, "Lokasi Simpan:", selected.getLokasiPenyimpanan() != null ? selected.getLokasiPenyimpanan() : "-");
        addDetailRow(grid, row++, "Tanggal Diterima:", selected.getTanggalDiterima() != null ? selected.getTanggalDiterima().toString() : "-");

        String khusus = selected.getDeskripsiKhusus();
        if (khusus != null && !khusus.trim().isEmpty()) {
            addDetailRow(grid, row++, "Atribut OOP Khusus:", khusus);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(380);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        mainContent.getChildren().addAll(photoBox, scrollPane);

        dialog.getDialogPane().setContent(mainContent);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 12px;");
        Label val = new Label(value);
        val.setWrapText(true);
        val.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12px;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    @FXML
    public void handleExportPdf() {
        if (dataList.isEmpty()) {
            AlertHelper.showWarning("Peringatan", "Tidak ada data untuk dicetak.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan Koleksi PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dokumen PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("Laporan_Koleksi_MuseumMate.pdf");

        File file = fileChooser.showSaveDialog(tableKoleksi.getScene().getWindow());
        if (file != null) {
            try {
                LaporanService.exportLaporanKoleksiPdf(dataList, file);
                AlertHelper.showInfo("Ekspor Berhasil", "Laporan PDF berhasil disimpan di:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                AlertHelper.showError("Ekspor Gagal", "Gagal menyimpan laporan PDF: " + e.getMessage());
            }
        }
    }

    private void bukaFormDialog(Koleksi koleksiToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/koleksi_form_dialog.fxml"));
            Parent root = loader.load();

            KoleksiFormController controller = loader.getController();
            controller.setKoleksi(koleksiToEdit);
            controller.setOnSuccessCallback(this::loadDataKoleksi);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(koleksiToEdit == null ? "Tambah Koleksi Baru" : "Ubah Data Koleksi");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableKoleksi.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Dialog", "Gagal membuka form input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatusLabel(int count) {
        lblStatusData.setText("Menampilkan " + count + " koleksi");
    }
}
