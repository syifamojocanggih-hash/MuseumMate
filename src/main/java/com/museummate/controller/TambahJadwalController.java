package com.museummate.controller;

import com.museummate.model.JadwalPerawatan;
import com.museummate.model.Koleksi;
import com.museummate.service.JadwalService;
import com.museummate.service.KoleksiService;
import com.museummate.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller untuk form input jadwal perawatan baru (PRD F5).
 */
public class TambahJadwalController {

    @FXML private ComboBox<Koleksi> cmbKoleksi;
    @FXML private DatePicker dpTanggalRencana;
    @FXML private TextField txtKegiatan;
    @FXML private TextArea txtKeterangan;

    private final JadwalService jadwalService = new JadwalService();
    private final KoleksiService koleksiService = new KoleksiService();
    private Runnable onSuccessCallback;

    @FXML
    public void initialize() {
        dpTanggalRencana.setValue(LocalDate.now().plusDays(7));
        try {
            List<Koleksi> list = koleksiService.getAllKoleksi();
            cmbKoleksi.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memuat daftar koleksi: " + e.getMessage());
        }
    }

    public void setKoleksiTarget(Koleksi target) {
        if (target != null) {
            for (Koleksi k : cmbKoleksi.getItems()) {
                if (k.getIdKoleksi() == target.getIdKoleksi()) {
                    cmbKoleksi.setValue(k);
                    cmbKoleksi.setDisable(true);
                    break;
                }
            }
        }
    }

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @FXML
    public void handleSimpan() {
        Koleksi selected = cmbKoleksi.getValue();
        if (selected == null) {
            AlertHelper.showWarning("Validasi", "Koleksi target wajib dipilih.");
            return;
        }

        LocalDate tgl = dpTanggalRencana.getValue();
        if (tgl == null) {
            AlertHelper.showWarning("Validasi", "Tanggal rencana wajib diisi.");
            return;
        }

        String kegiatan = txtKegiatan.getText();
        if (kegiatan == null || kegiatan.trim().isEmpty()) {
            AlertHelper.showWarning("Validasi", "Nama kegiatan perawatan wajib diisi.");
            return;
        }

        try {
            JadwalPerawatan j = new JadwalPerawatan(
                    selected.getIdKoleksi(),
                    tgl,
                    kegiatan.trim(),
                    txtKeterangan.getText() != null ? txtKeterangan.getText().trim() : ""
            );

            jadwalService.tambahJadwal(j);
            com.museummate.service.LogAktivitasService.catat("TAMBAH", "jadwal_perawatan", 
                    "Menambahkan jadwal perawatan untuk " + selected.getKodeRegistrasi() + " (" + selected.getNama() + "): " + kegiatan.trim());
            AlertHelper.showInfo("Sukses", "Jadwal perawatan berhasil disimpan.");

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }

            tutupDialog();
        } catch (Exception e) {
            AlertHelper.showError("Kesalahan", "Gagal menyimpan jadwal: " + e.getMessage());
        }
    }

    @FXML
    public void handleBatal() {
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) cmbKoleksi.getScene().getWindow();
        stage.close();
    }
}
