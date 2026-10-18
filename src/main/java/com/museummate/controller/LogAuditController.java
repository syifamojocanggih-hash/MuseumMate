package com.museummate.controller;

import com.museummate.model.LogAktivitas;
import com.museummate.service.LogAktivitasService;
import com.museummate.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk tampilan Log Audit Aktivitas Sistem (PRD F10 / UC-13).
 * Khusus diperuntukkan bagi Administrator untuk penelusuran audit dan akuntabilitas.
 */
public class LogAuditController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbAksiFilter;
    @FXML private ComboBox<String> cmbTabelFilter;

    @FXML private TableView<LogAktivitas> tableLog;
    @FXML private TableColumn<LogAktivitas, String> colWaktu;
    @FXML private TableColumn<LogAktivitas, String> colPengguna;
    @FXML private TableColumn<LogAktivitas, String> colAksi;
    @FXML private TableColumn<LogAktivitas, String> colTabel;
    @FXML private TableColumn<LogAktivitas, String> colKeterangan;

    @FXML private Label lblStatusLog;

    private final LogAktivitasService logService = new LogAktivitasService();
    private final ObservableList<LogAktivitas> logList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupFilterOptions();
        setupTableColumns();
        loadDataLog();
    }

    private void setupFilterOptions() {
        cmbAksiFilter.setItems(FXCollections.observableArrayList("SEMUA", "TAMBAH", "UBAH", "HAPUS", "LOGIN", "LOGOUT"));
        cmbAksiFilter.setValue("SEMUA");

        cmbTabelFilter.setItems(FXCollections.observableArrayList("SEMUA", "koleksi", "pengguna", "jadwal_perawatan", "pemeriksaan"));
        cmbTabelFilter.setValue("SEMUA");
    }

    private void setupTableColumns() {
        colWaktu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWaktuFormatted()));
        colPengguna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaPengguna()));
        colAksi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJenisAksi()));
        colTabel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTabelTerkait()));
        colKeterangan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKeterangan()));

        // Styling Badge Aksi modern
        colAksi.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    String styleClass = switch (item.toUpperCase()) {
                        case "TAMBAH" -> "badge-tambah";
                        case "UBAH" -> "badge-ubah";
                        case "HAPUS" -> "badge-hapus";
                        case "LOGIN" -> "badge-login";
                        default -> "badge-default";
                    };
                    badge.getStyleClass().add(styleClass);
                    setText(null);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    @FXML
    public void loadDataLog() {
        try {
            String keyword = txtSearch != null ? txtSearch.getText() : "";
            String aksi = cmbAksiFilter != null ? cmbAksiFilter.getValue() : "SEMUA";
            String tabel = cmbTabelFilter != null ? cmbTabelFilter.getValue() : "SEMUA";

            List<LogAktivitas> data = logService.filterLogs(keyword, aksi, tabel);
            logList.setAll(data);
            tableLog.setItems(logList);

            if (lblStatusLog != null) {
                lblStatusLog.setText("Menampilkan " + data.size() + " catatan aktivitas log audit.");
            }
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Basis Data", "Gagal memuat log audit: " + e.getMessage());
        }
    }

    @FXML
    public void handleFilterChanged() {
        loadDataLog();
    }

    @FXML
    public void handleResetFilter() {
        if (txtSearch != null) txtSearch.clear();
        if (cmbAksiFilter != null) cmbAksiFilter.setValue("SEMUA");
        if (cmbTabelFilter != null) cmbTabelFilter.setValue("SEMUA");
        loadDataLog();
    }

    @FXML
    public void handleRefresh() {
        loadDataLog();
    }
}
