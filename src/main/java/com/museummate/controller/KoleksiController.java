package com.museummate.controller;

import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.service.KoleksiService;
import com.museummate.service.LaporanService;
import com.museummate.util.AlertHelper;
import com.museummate.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    public void handleLihatDetail() {
        Koleksi selected = tableKoleksi.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih salah satu koleksi untuk melihat rincian.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Kode Registrasi: ").append(selected.getKodeRegistrasi()).append("\n");
        sb.append("Nama Benda: ").append(selected.getNama()).append("\n");
        sb.append("Kategori: ").append(selected.getKategori().getLabel()).append("\n");
        sb.append("Kondisi Fisik: ").append(selected.getKondisi().getLabel()).append("\n");
        sb.append("Asal Daerah: ").append(selected.getAsal()).append("\n");
        sb.append("Tahun Perkiraan: ").append(selected.getTahunPerkiraan()).append("\n");
        sb.append("Bahan: ").append(selected.getBahan()).append("\n");
        sb.append("Sumber Perolehan: ").append(selected.getSumberPerolehan()).append("\n");
        sb.append("Lokasi Simpan: ").append(selected.getLokasiPenyimpanan()).append("\n");
        sb.append("Tanggal Diterima: ").append(selected.getTanggalDiterima()).append("\n\n");
        sb.append("--- Atribut Khusus Kategori (OOP) ---\n");
        sb.append(selected.getDeskripsiKhusus().isEmpty() ? "Tidak ada atribut khusus." : selected.getDeskripsiKhusus());

        AlertHelper.showInfo("Rincian Lengkap Koleksi", sb.toString());
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
