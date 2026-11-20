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
 * Menghadirkan tabel inventaris, filter interaktif, side preview panel foto fisik,
 * indikator status perawatan terintegrasi, dan pengelolaan foto benda cagar budaya.
 */
public class KoleksiController {

    @FXML private Label lblPageTitle;
    @FXML private Label lblPageSubtitle;

    @FXML private TextField txtSearch;
    @FXML private HBox boxKategoriFilter;
    @FXML private ComboBox<KategoriKoleksi> cmbKategoriFilter;
    @FXML private ComboBox<KondisiKoleksi> cmbKondisiFilter;

    @FXML private Button btnTambah;
    @FXML private Button btnUbah;
    @FXML private Button btnHapus;
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

    // Side Preview & Indikator Status Perawatan Panel
    @FXML private VBox panelPreviewKoleksi;
    @FXML private Label lblPreviewSub;
    @FXML private Label lblPreviewKategoriBadge;
    @FXML private StackPane framePreviewFoto;
    @FXML private ImageView imgPreviewKoleksi;
    @FXML private VBox boxNoPhotoPlaceholder;
    @FXML private Button btnGantiFotoSide;
    @FXML private VBox boxIndikatorPerawatan;
    @FXML private Label lblIndikatorIcon;
    @FXML private Label lblIndikatorTitle;
    @FXML private Label lblIndikatorDesc;
    @FXML private Button btnQuickTindakan;
    @FXML private Label lblPreviewNama;
    @FXML private Label lblPreviewLokasi;
    @FXML private Label lblPreviewBahan;

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

        // Filter kategori disembunyikan untuk Konservator karena Konservator sudah dikhususkan per kategori
        if (boxKategoriFilter != null) {
            boxKategoriFilter.setVisible(isAdmin);
            boxKategoriFilter.setManaged(isAdmin);
        }

        // btnUploadFoto dapat diakses oleh Konservator maupun Admin
        if (btnUploadFoto != null) {
            btnUploadFoto.setVisible(true);
            btnUploadFoto.setManaged(true);
        }

        // Pengaturan spesialisasi peran konservator
        if (SessionManager.getInstance().hasSpesialisasi()) {
            KategoriKoleksi spec = SessionManager.getInstance().getKategoriSpesialisasi();
            if (spec != null) {
                if (lblPageTitle != null) {
                    lblPageTitle.setText("Manajemen Koleksi: " + spec.getLabel());
                }
                if (lblPageSubtitle != null) {
                    lblPageSubtitle.setText("Inventarisasi, pemantauan fisik, dan kurasi dokumentasi foto khusus kategori " + spec.getLabel() + ".");
                }
            }
        }
    }

    private void setupFilterControls() {
        cmbKategoriFilter.getItems().add(null); // Option "Semua"
        cmbKategoriFilter.getItems().addAll(KategoriKoleksi.values());

        cmbKondisiFilter.getItems().add(null); // Option "Semua"
        cmbKondisiFilter.getItems().addAll(KondisiKoleksi.values());

        // Jika konservator memiliki spesialisasi, filter default disesuaikan
        if (SessionManager.getInstance().hasSpesialisasi()) {
            KategoriKoleksi spec = SessionManager.getInstance().getKategoriSpesialisasi();
            if (spec != null) {
                cmbKategoriFilter.setValue(spec);
            }
        }

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

        // Listener saat item pada tabel dipilih untuk sinkronisasi side preview panel
        tableKoleksi.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            updateSidePreview(selected);
        });
    }

    public void loadDataKoleksi() {
        try {
            List<Koleksi> all = koleksiService.getAllKoleksi();

            // Jika konservator memiliki spesialisasi, batasi data awal hanya pada spesialisasinya
            if (SessionManager.getInstance().hasSpesialisasi()) {
                KategoriKoleksi spec = SessionManager.getInstance().getKategoriSpesialisasi();
                if (spec != null) {
                    all = all.stream().filter(k -> k.getKategori() == spec).toList();
                }
            }

            dataList.setAll(all);
            updateStatusLabel(all.size());

            if (!dataList.isEmpty()) {
                tableKoleksi.getSelectionModel().select(0);
            } else {
                updateSidePreview(null);
            }
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

            if (SessionManager.getInstance().hasSpesialisasi() && kategori == null) {
                KategoriKoleksi spec = SessionManager.getInstance().getKategoriSpesialisasi();
                if (spec != null) {
                    hasil = hasil.stream().filter(k -> k.getKategori() == spec).toList();
                }
            }

            dataList.setAll(hasil);
            updateStatusLabel(hasil.size());

            if (!dataList.isEmpty()) {
                tableKoleksi.getSelectionModel().select(0);
            } else {
                updateSidePreview(null);
            }
        } catch (SQLException e) {
            AlertHelper.showError("Pencarian Gagal", "Terjadi kesalahan saat memfilter data: " + e.getMessage());
        }
    }

    /**
     * Memperbarui visual Side Preview Panel sesuai koleksi yang sedang dipilih.
     * Menampilkan foto fisik, indikator kondisi (Butuh Segera Dirawat / Perlu Pembersihan / Bagus),
     * serta tombol tindakan cepat konservasi.
     */
    private void updateSidePreview(Koleksi k) {
        if (panelPreviewKoleksi == null) return;

        if (k == null) {
            if (lblPreviewSub != null) lblPreviewSub.setText("Pilih koleksi untuk melihat pratinjau");
            if (lblPreviewKategoriBadge != null) lblPreviewKategoriBadge.setText("MUSEUM");
            if (imgPreviewKoleksi != null) imgPreviewKoleksi.setImage(null);
            if (boxNoPhotoPlaceholder != null) boxNoPhotoPlaceholder.setVisible(true);
            if (lblPreviewNama != null) lblPreviewNama.setText("Belum ada koleksi terpilih");
            if (lblPreviewLokasi != null) lblPreviewLokasi.setText("Pilih salah satu baris tabel di sebelah kiri.");
            if (lblPreviewBahan != null) lblPreviewBahan.setText("");
            if (boxIndikatorPerawatan != null) boxIndikatorPerawatan.setVisible(false);
            return;
        }

        if (lblPreviewSub != null) lblPreviewSub.setText(k.getKodeRegistrasi());
        if (lblPreviewKategoriBadge != null) {
            lblPreviewKategoriBadge.setText(k.getKategori().getLabel().toUpperCase());
            lblPreviewKategoriBadge.setStyle(switch (k.getKategori()) {
                case ARTEFAK -> "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-padding: 2px 7px; -fx-background-radius: 6px;";
                case NASKAH -> "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-padding: 2px 7px; -fx-background-radius: 6px;";
                case LUKISAN -> "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 2px 7px; -fx-background-radius: 6px;";
                case FOSIL -> "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: #f3e8ff; -fx-text-fill: #7e22ce; -fx-padding: 2px 7px; -fx-background-radius: 6px;";
            });
        }

        if (lblPreviewNama != null) lblPreviewNama.setText(k.getNama());
        if (lblPreviewLokasi != null) lblPreviewLokasi.setText("Lokasi: " + (k.getLokasiPenyimpanan() != null ? k.getLokasiPenyimpanan() : "-"));
        if (lblPreviewBahan != null) lblPreviewBahan.setText("Bahan: " + (k.getBahan() != null ? k.getBahan() : "-") + " | Asal: " + (k.getAsal() != null ? k.getAsal() : "-"));

        // Render Foto Fisik
        Image img = muatGambar(k.getFotoPath());
        if (img != null) {
            if (imgPreviewKoleksi != null) {
                imgPreviewKoleksi.setImage(img);
                imgPreviewKoleksi.setVisible(true);
            }
            if (boxNoPhotoPlaceholder != null) boxNoPhotoPlaceholder.setVisible(false);
        } else {
            if (imgPreviewKoleksi != null) {
                imgPreviewKoleksi.setImage(null);
                imgPreviewKoleksi.setVisible(false);
            }
            if (boxNoPhotoPlaceholder != null) boxNoPhotoPlaceholder.setVisible(true);
        }

        // Render Indikator Status Perawatan Fisik
        if (boxIndikatorPerawatan != null) {
            boxIndikatorPerawatan.setVisible(true);
            KondisiKoleksi kondisi = k.getKondisi();

            if (kondisi == KondisiKoleksi.RUSAK_BERAT || kondisi == KondisiKoleksi.RUSAK_RINGAN) {
                boxIndikatorPerawatan.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-width: 1.5px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 9px 11px;");
                if (lblIndikatorIcon != null) lblIndikatorIcon.setText("🔴");
                if (lblIndikatorTitle != null) {
                    lblIndikatorTitle.setText("BUTUH SEGERA DIRAWAT");
                    lblIndikatorTitle.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 800; -fx-text-fill: #dc2626;");
                }
                if (lblIndikatorDesc != null) {
                    lblIndikatorDesc.setText("Fisik koleksi terindikasi mengalami kerusakan (" + kondisi.getLabel() + "). Tindakan restorasi konservasi mendesak!");
                }
            } else if (kondisi == KondisiKoleksi.CUKUP) {
                boxIndikatorPerawatan.setStyle("-fx-background-color: #fffbeb; -fx-border-color: #fde68a; -fx-border-width: 1.5px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 9px 11px;");
                if (lblIndikatorIcon != null) lblIndikatorIcon.setText("🟡");
                if (lblIndikatorTitle != null) {
                    lblIndikatorTitle.setText("PERLU PEMBERSIHAN & PRESERVASI");
                    lblIndikatorTitle.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 800; -fx-text-fill: #d97706;");
                }
                if (lblIndikatorDesc != null) {
                    lblIndikatorDesc.setText("Kondisi fisik cukup stabil, namun memerlukan pembersihan debu berkala atau stabilisasi kelembaban.");
                }
            } else {
                boxIndikatorPerawatan.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-width: 1.5px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 9px 11px;");
                if (lblIndikatorIcon != null) lblIndikatorIcon.setText("🟢");
                if (lblIndikatorTitle != null) {
                    lblIndikatorTitle.setText("KONDISI BAGUS / PRIMA");
                    lblIndikatorTitle.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 800; -fx-text-fill: #16a34a;");
                }
                if (lblIndikatorDesc != null) {
                    lblIndikatorDesc.setText("Benda koleksi dalam keadaan prima, terawat baik, dan aman dalam ruang penyimpanan/pameran.");
                }
            }

            // Aksi Cepat berdasarkan Role: Admin -> Buat Jadwal (UC-04), Konservator -> Catat Pemeriksaan (UC-06)
            if (btnQuickTindakan != null) {
                boolean isAdmin = SessionManager.getInstance().isAdmin();
                if (isAdmin) {
                    btnQuickTindakan.setText("📅 Buat Jadwal Perawatan");
                    btnQuickTindakan.setStyle("-fx-background-color: #174C78; -fx-text-fill: white; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 8px; -fx-cursor: hand;");
                } else {
                    btnQuickTindakan.setText("📋 Catat Pemeriksaan Fisik");
                    btnQuickTindakan.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 8px; -fx-cursor: hand;");
                }
            }
        }
    }

    @FXML
    public void handleQuickTindakan() {
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih data koleksi terlebih dahulu.");
            return;
        }

        if (SessionManager.getInstance().isAdmin()) {
            bukaFormTambahJadwal(selected);
        } else {
            bukaFormCatatPemeriksaan(selected);
        }
    }

    private void bukaFormCatatPemeriksaan(Koleksi koleksi) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/catat_pemeriksaan_dialog.fxml"));
            Parent root = loader.load();

            CatatPemeriksaanController controller = loader.getController();
            controller.setKoleksiTarget(koleksi);
            controller.setOnSuccessCallback(this::loadDataKoleksi);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Catat Pemeriksaan Fisik");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableKoleksi.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Dialog", "Gagal membuka form pemeriksaan: " + e.getMessage());
        }
    }

    private void bukaFormTambahJadwal(Koleksi koleksi) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/tambah_jadwal_dialog.fxml"));
            Parent root = loader.load();

            TambahJadwalController controller = loader.getController();
            controller.setKoleksiTarget(koleksi);
            controller.setOnSuccessCallback(this::loadDataKoleksi);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Tambah Rencana Perawatan");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableKoleksi.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Dialog", "Gagal membuka form jadwal: " + e.getMessage());
        }
    }

    @FXML
    public void handleResetFilter() {
        txtSearch.clear();
        if (SessionManager.getInstance().hasSpesialisasi()) {
            cmbKategoriFilter.setValue(SessionManager.getInstance().getKategoriSpesialisasi());
        } else {
            cmbKategoriFilter.setValue(null);
        }
        cmbKondisiFilter.setValue(null);
        loadDataKoleksi();
    }

    @FXML
    public void handleTambahKoleksi() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertHelper.showWarning("Akses Ditolak", "Hanya Administrator yang memiliki wewenang untuk menambah data koleksi baru (UC-03).");
            return;
        }
        bukaFormDialog(null);
    }

    @FXML
    public void handleUbahKoleksi() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertHelper.showWarning("Akses Ditolak", "Hanya Administrator yang memiliki wewenang untuk mengubah data koleksi (UC-03).");
            return;
        }
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih data koleksi yang ingin diubah terlebih dahulu.");
            return;
        }
        bukaFormDialog(selected);
    }

    @FXML
    public void handleHapusKoleksi() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertHelper.showWarning("Akses Ditolak", "Hanya Administrator yang memiliki wewenang untuk menghapus data koleksi (UC-03).");
            return;
        }
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih data koleksi yang ingin dihapus terlebih dahulu.");
            return;
        }

        try {
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
                updateSidePreview(selected);
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
