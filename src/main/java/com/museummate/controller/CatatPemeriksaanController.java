package com.museummate.controller;

import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.Pemeriksaan;
import com.museummate.service.KoleksiService;
import com.museummate.service.PemeriksaanService;
import com.museummate.util.AlertHelper;
import com.museummate.util.SessionManager;
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
 * Controller untuk dialog pencatatan pemeriksaan fisik koleksi (PRD F4).
 */
public class CatatPemeriksaanController {

    @FXML private ComboBox<Koleksi> cmbKoleksi;
    @FXML private DatePicker dpTanggal;
    @FXML private ComboBox<KondisiKoleksi> cmbKondisiHasil;
    @FXML private TextField txtTindakan;
    @FXML private TextArea txtCatatan;

    private final PemeriksaanService pemeriksaanService = new PemeriksaanService();
    private final KoleksiService koleksiService = new KoleksiService();
    private Runnable onSuccessCallback;

    @FXML
    public void initialize() {
        dpTanggal.setValue(LocalDate.now());
        cmbKondisiHasil.getItems().addAll(KondisiKoleksi.values());
        cmbKondisiHasil.setValue(KondisiKoleksi.BAIK);

        try {
            List<Koleksi> list = koleksiService.getAllKoleksi();
            cmbKoleksi.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memuat koleksi: " + e.getMessage());
        }
    }

    public void setKoleksiTarget(Koleksi target) {
        if (target != null) {
            for (Koleksi k : cmbKoleksi.getItems()) {
                if (k.getIdKoleksi() == target.getIdKoleksi()) {
                    cmbKoleksi.setValue(k);
                    cmbKoleksi.setDisable(true);
                    cmbKondisiHasil.setValue(target.getKondisi());
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
            AlertHelper.showWarning("Validasi", "Koleksi yang diperiksa wajib dipilih.");
            return;
        }

        LocalDate tgl = dpTanggal.getValue();
        if (tgl == null) {
            AlertHelper.showWarning("Validasi", "Tanggal pemeriksaan wajib diisi.");
            return;
        }

        if (tgl.isAfter(LocalDate.now())) {
            AlertHelper.showWarning("Validasi", "Tanggal pemeriksaan tidak boleh di masa depan (PRD F4).");
            return;
        }

        KondisiKoleksi kondisi = cmbKondisiHasil.getValue();
        if (kondisi == null) {
            AlertHelper.showWarning("Validasi", "Kondisi hasil pemeriksaan wajib ditentukan.");
            return;
        }

        String tindakan = txtTindakan.getText();
        if (tindakan == null || tindakan.trim().isEmpty()) {
            AlertHelper.showWarning("Validasi", "Tindakan pemeriksaan wajib diisi.");
            return;
        }

        try {
            Pemeriksaan p = new Pemeriksaan(
                    selected.getIdKoleksi(),
                    SessionManager.getInstance().isLoggedIn() ? SessionManager.getInstance().getCurrentUser().getIdPengguna() : 1,
                    tgl,
                    kondisi,
                    tindakan.trim(),
                    txtCatatan.getText() != null ? txtCatatan.getText().trim() : ""
            );

            pemeriksaanService.catatPemeriksaan(p);
            com.museummate.service.LogAktivitasService.catat("TAMBAH", "pemeriksaan", 
                    "Mencatat pemeriksaan koleksi: " + selected.getKodeRegistrasi() + " - " + selected.getNama() + 
                    " (Kondisi baru: " + kondisi.getLabel() + ")");
            AlertHelper.showInfo("Sukses", "Pemeriksaan berhasil dicatat dan kondisi koleksi telah diperbarui.");

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }

            tutupDialog();
        } catch (Exception e) {
            AlertHelper.showError("Kesalahan", "Gagal menyimpan pemeriksaan: " + e.getMessage());
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
