package com.museummate.controller;

import com.museummate.dao.PenggunaDAO;
import com.museummate.model.Pengguna;
import com.museummate.model.PeranPengguna;
import com.museummate.util.AlertHelper;
import com.museummate.util.DateUtil;
import com.museummate.util.PasswordUtil;
import com.museummate.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller untuk pengelolaan akun pengguna khusus Administrator (PRD F1).
 */
public class PenggunaController {

    @FXML private TableView<Pengguna> tablePengguna;
    @FXML private TableColumn<Pengguna, String> colId;
    @FXML private TableColumn<Pengguna, String> colUsername;
    @FXML private TableColumn<Pengguna, String> colNamaLengkap;
    @FXML private TableColumn<Pengguna, String> colPeran;
    @FXML private TableColumn<Pengguna, String> colDibuatPada;

    private final PenggunaDAO penggunaDAO = new PenggunaDAO();
    private final ObservableList<Pengguna> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdPengguna())));
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colNamaLengkap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaLengkap()));
        colPeran.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPeran().getLabel()));
        colDibuatPada.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDateTime(c.getValue().getDibuatPada())));

        tablePengguna.setItems(userList);
        loadDataPengguna();
    }

    public void loadDataPengguna() {
        try {
            List<Pengguna> list = penggunaDAO.findAll();
            userList.setAll(list);
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan", "Gagal memuat pengguna: " + e.getMessage());
        }
    }

    @FXML
    public void handleTambahPengguna() {
        Dialog<Pengguna> dialog = createPenggunaDialog(null);
        Optional<Pengguna> result = dialog.showAndWait();
        result.ifPresent(user -> {
            try {
                penggunaDAO.save(user);
                com.museummate.service.LogAktivitasService.catat("TAMBAH", "pengguna", 
                        "Menambahkan akun pengguna baru: " + user.getUsername() + " (" + user.getPeran().getLabel() + ")");
                AlertHelper.showInfo("Sukses", "Pengguna baru '" + user.getUsername() + "' berhasil didaftarkan.");
                loadDataPengguna();
            } catch (SQLException e) {
                AlertHelper.showError("Gagal Mendaftar", "Terjadi kesalahan: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleUbahPengguna() {
        Pengguna selected = tablePengguna.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih akun yang ingin diubah.");
            return;
        }

        Dialog<Pengguna> dialog = createPenggunaDialog(selected);
        Optional<Pengguna> result = dialog.showAndWait();
        result.ifPresent(user -> {
            try {
                boolean updatePassword = user.getPasswordHash() != null && !user.getPasswordHash().isEmpty();
                penggunaDAO.update(user, updatePassword);
                com.museummate.service.LogAktivitasService.catat("UBAH", "pengguna", 
                        "Memperbarui profil akun pengguna: " + user.getUsername() + " (" + user.getNamaLengkap() + ")");
                AlertHelper.showInfo("Sukses", "Data akun pengguna berhasil diperbarui.");
                loadDataPengguna();
            } catch (SQLException e) {
                AlertHelper.showError("Gagal Memperbarui", "Terjadi kesalahan: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleHapusPengguna() {
        Pengguna selected = tablePengguna.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Peringatan", "Pilih akun yang ingin dihapus.");
            return;
        }

        // Cegah menghapus akun sendiri
        if (SessionManager.getInstance().isLoggedIn() &&
            SessionManager.getInstance().getCurrentUser().getIdPengguna() == selected.getIdPengguna()) {
            AlertHelper.showWarning("Operasi Dilarang", "Anda tidak dapat menghapus akun yang sedang aktif digunakan.");
            return;
        }

        boolean konfirmasi = AlertHelper.showConfirmation("Konfirmasi Hapus Akun", "Hapus Pengguna",
                "Apakah Anda yakin ingin menghapus akun '" + selected.getUsername() + "' (" + selected.getNamaLengkap() + ")?\n" +
                "Catatan: Riwayat pemeriksaan yang pernah dicatat oleh akun ini tetap tersimpan aman di basis data (PRD F1).");
        if (konfirmasi) {
            try {
                penggunaDAO.delete(selected.getIdPengguna());
                com.museummate.service.LogAktivitasService.catat("HAPUS", "pengguna", 
                        "Menghapus akun pengguna: " + selected.getUsername() + " (" + selected.getNamaLengkap() + ")");
                AlertHelper.showInfo("Sukses", "Akun pengguna berhasil dihapus.");
                loadDataPengguna();
            } catch (SQLException e) {
                AlertHelper.showError("Gagal Menghapus", "Terjadi kesalahan: " + e.getMessage());
            }
        }
    }

    private Dialog<Pengguna> createPenggunaDialog(Pengguna toEdit) {
        Dialog<Pengguna> dialog = new Dialog<>();
        dialog.setTitle(toEdit == null ? "Tambah Pengguna Baru" : "Ubah Data Pengguna");
        dialog.setHeaderText(toEdit == null ? "Masukkan data akun baru:" : "Perbarui data akun:");

        ButtonType btnSimpanType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpanType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField txtUsername = new TextField(toEdit != null ? toEdit.getUsername() : "");
        TextField txtNama = new TextField(toEdit != null ? toEdit.getNamaLengkap() : "");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(toEdit != null ? "(Biarkan kosong jika tidak ingin ganti)" : "Password baru");

        ComboBox<PeranPengguna> cmbPeran = new ComboBox<>();
        cmbPeran.getItems().addAll(PeranPengguna.values());
        cmbPeran.setValue(toEdit != null ? toEdit.getPeran() : PeranPengguna.KONSERVATOR);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Nama Lengkap:"), 0, 1);
        grid.add(txtNama, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(txtPassword, 1, 2);
        grid.add(new Label("Hak Akses / Peran:"), 0, 3);
        grid.add(cmbPeran, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSimpanType) {
                String u = txtUsername.getText().trim();
                String n = txtNama.getText().trim();
                String p = txtPassword.getText();

                if (u.isEmpty() || n.isEmpty() || (toEdit == null && p.isEmpty())) {
                    AlertHelper.showWarning("Validasi", "Username, Nama Lengkap, dan Password wajib diisi.");
                    return null;
                }

                Pengguna target = (toEdit != null) ? toEdit : new Pengguna();
                target.setUsername(u);
                target.setNamaLengkap(n);
                target.setPeran(cmbPeran.getValue());

                if (!p.isEmpty()) {
                    target.setPasswordHash(PasswordUtil.hashPassword(p));
                }

                return target;
            }
            return null;
        });

        return dialog;
    }
}
