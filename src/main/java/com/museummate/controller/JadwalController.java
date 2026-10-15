package com.museummate.controller;

import com.museummate.model.JadwalPerawatan;
import com.museummate.model.StatusJadwal;
import com.museummate.service.JadwalService;
import com.museummate.util.AlertHelper;
import com.museummate.util.DateUtil;
import com.museummate.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk pengelolaan jadwal perawatan museum (PRD F5).
 */
public class JadwalController {

    @FXML private HBox boxAlertTerlambat;
    @FXML private Label lblAlertTerlambatText;

    @FXML private Button btnTambahJadwal;
    @FXML private Button btnSelesai;
    @FXML private Button btnBatal;
    @FXML private Button btnHapus;

    @FXML private TableView<JadwalPerawatan> tableJadwal;
    @FXML private TableColumn<JadwalPerawatan, String> colTanggalRencana;
    @FXML private TableColumn<JadwalPerawatan, String> colStatusVisual;
    @FXML private TableColumn<JadwalPerawatan, String> colKode;
    @FXML private TableColumn<JadwalPerawatan, String> colNama;
    @FXML private TableColumn<JadwalPerawatan, String> colKegiatan;
    @FXML private TableColumn<JadwalPerawatan, String> colKeterangan;

    private final JadwalService jadwalService = new JadwalService();
    private final ObservableList<JadwalPerawatan> jadwalList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupRoleAccess();
        setupTableColumns();
        loadDataJadwal();
    }

    private void setupRoleAccess() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        btnTambahJadwal.setVisible(isAdmin);
        btnTambahJadwal.setManaged(isAdmin);
        btnSelesai.setVisible(isAdmin);
        btnSelesai.setManaged(isAdmin);
        btnBatal.setVisible(isAdmin);
        btnBatal.setManaged(isAdmin);
        btnHapus.setVisible(isAdmin);
        btnHapus.setManaged(isAdmin);
    }

    private void setupTableColumns() {
        colTanggalRencana.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatIndonesian(c.getValue().getTanggalRencana())));
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKodeKoleksi()));
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaKoleksi()));
        colKegiatan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKegiatan()));
        colKeterangan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKeterangan()));

        colStatusVisual.setCellValueFactory(c -> {
            JadwalPerawatan j = c.getValue();
            if (j.isTerlambat()) {
                return new SimpleStringProperty("TERLAMBAT");
            }
            return new SimpleStringProperty(j.getStatus().getLabel().toUpperCase());
        });

        // Visual highlight terlambat (PRD F5)
        colStatusVisual.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("TERLAMBAT".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if ("SELESAI".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-alignment: CENTER;");
                    } else if ("DIBATALKAN".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #64748b; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #2563eb; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        tableJadwal.setItems(jadwalList);
    }

    public void loadDataJadwal() {
        try {
            List<JadwalPerawatan> list = jadwalService.getAllJadwal();
            jadwalList.setAll(list);

            int terlambat = jadwalService.getJumlahTerlambat();
            if (terlambat > 0) {
                boxAlertTerlambat.setVisible(true);
                boxAlertTerlambat.setManaged(true);
                lblAlertTerlambatText.setText("Terdapat " + terlambat + " jadwal perawatan yang telah melewati batas waktu (TERLAMBAT).");
            } else {
                boxAlertTerlambat.setVisible(false);
                boxAlertTerlambat.setManaged(false);
            }
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Data", "Gagal memuat jadwal: " + e.getMessage());
        }
    }

    @FXML
    public void handleTambahJadwal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/tambah_jadwal_dialog.fxml"));
            Parent root = loader.load();

            TambahJadwalController controller = loader.getController();
            controller.setOnSuccessCallback(this::loadDataJadwal);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Susun Jadwal Perawatan Baru");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableJadwal.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Dialog", "Gagal membuka form jadwal: " + e.getMessage());
        }
    }

    @FXML
    public void handleTandaiSelesai() {
        JadwalPerawatan selected = tableJadwal.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih salah satu jadwal perawatan.");
            return;
        }

        try {
            jadwalService.ubahStatus(selected.getIdJadwal(), StatusJadwal.SELESAI);
            AlertHelper.showInfo("Sukses", "Status jadwal berhasil diperbarui menjadi SELESAI.");
            loadDataJadwal();
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memperbarui status: " + e.getMessage());
        }
    }

    @FXML
    public void handleBatalkanJadwal() {
        JadwalPerawatan selected = tableJadwal.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih salah satu jadwal perawatan.");
            return;
        }

        try {
            jadwalService.ubahStatus(selected.getIdJadwal(), StatusJadwal.DIBATALKAN);
            AlertHelper.showInfo("Sukses", "Jadwal berhasil dibatalkan.");
            loadDataJadwal();
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memperbarui status: " + e.getMessage());
        }
    }

    @FXML
    public void handleHapusJadwal() {
        JadwalPerawatan selected = tableJadwal.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih jadwal yang ingin dihapus.");
            return;
        }

        boolean konfirmasi = AlertHelper.showConfirmation("Konfirmasi Hapus", "Hapus Jadwal Perawatan",
                "Apakah Anda yakin ingin menghapus jadwal untuk '" + selected.getNamaKoleksi() + "'?");
        if (konfirmasi) {
            try {
                jadwalService.hapusJadwal(selected.getIdJadwal());
                AlertHelper.showInfo("Sukses", "Jadwal perawatan berhasil dihapus.");
                loadDataJadwal();
            } catch (SQLException e) {
                AlertHelper.showError("Kesalahan", "Gagal menghapus jadwal: " + e.getMessage());
            }
        }
    }
}
