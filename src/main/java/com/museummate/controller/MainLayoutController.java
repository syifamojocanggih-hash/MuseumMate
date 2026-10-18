package com.museummate.controller;

import com.museummate.model.Pengguna;
import com.museummate.util.AlertHelper;
import com.museummate.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller utama pengatur tata letak (Sidebar & Navigasi Antar Halaman).
 * Menyesuaikan visibilitas menu sesuai peran pengguna aktif (PRD F1).
 */
public class MainLayoutController {

    @FXML private Label lblNamaPengguna;
    @FXML private Label lblPeranPengguna;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavKoleksi;
    @FXML private Button btnNavPrioritas;
    @FXML private Button btnNavPemeriksaan;
    @FXML private Button btnNavJadwal;
    @FXML private Button btnNavNotifikasi;
    @FXML private Button btnNavLogAudit;
    @FXML private Button btnNavPengguna;

    @FXML private StackPane contentArea;

    private Button activeButton;
    private final com.museummate.service.JadwalService jadwalService = new com.museummate.service.JadwalService();

    @FXML
    public void initialize() {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblNamaPengguna.setText(user.getNamaLengkap());
            lblPeranPengguna.setText(user.getPeran().getLabel().toUpperCase());

            // Menu Pengguna dan Log Audit hanya untuk Admin (PRD F1 / F10)
            boolean isAdmin = user.isAdmin();
            btnNavPengguna.setVisible(isAdmin);
            btnNavPengguna.setManaged(isAdmin);

            btnNavLogAudit.setVisible(isAdmin);
            btnNavLogAudit.setManaged(isAdmin);
        }

        updateNotifikasiBadge();

        // Tampilkan Dashboard sebagai halaman awal
        showDashboard();
    }

    private void updateNotifikasiBadge() {
        try {
            int notifCount = jadwalService.getJumlahNotifikasiAktif();
            if (notifCount > 0) {
                btnNavNotifikasi.setText("🔔  Notifikasi Jadwal (" + notifCount + ")");
            } else {
                btnNavNotifikasi.setText("🔔  Notifikasi Jadwal (F8)");
            }
        } catch (Exception e) {
            btnNavNotifikasi.setText("🔔  Notifikasi Jadwal (F8)");
        }
    }

    @FXML
    public void showDashboard() {
        setActiveNav(btnNavDashboard);
        loadView("/com/museummate/view/dashboard.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void showKoleksi() {
        setActiveNav(btnNavKoleksi);
        loadView("/com/museummate/view/koleksi_view.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void showPrioritas() {
        setActiveNav(btnNavPrioritas);
        loadView("/com/museummate/view/prioritas_view.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void showPemeriksaan() {
        setActiveNav(btnNavPemeriksaan);
        loadView("/com/museummate/view/pemeriksaan_view.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void showJadwal() {
        setActiveNav(btnNavJadwal);
        loadView("/com/museummate/view/jadwal_view.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void showNotifikasi() {
        setActiveNav(btnNavNotifikasi);
        loadView("/com/museummate/view/notifikasi_view.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void showLogAudit() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertHelper.showWarning("Akses Ditolak", "Hanya Administrator yang memiliki akses ke log audit aktivitas.");
            return;
        }
        setActiveNav(btnNavLogAudit);
        loadView("/com/museummate/view/log_audit_view.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void showPengguna() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertHelper.showWarning("Akses Ditolak", "Hanya Administrator yang memiliki akses ke manajemen akun pengguna.");
            return;
        }
        setActiveNav(btnNavPengguna);
        loadView("/com/museummate/view/pengguna_view.fxml");
        updateNotifikasiBadge();
    }

    @FXML
    public void handleLogout() {
        boolean yakin = AlertHelper.showConfirmation("Konfirmasi Keluar", "Apakah Anda yakin ingin keluar?", "Sesi aktif akan diakhiri.");
        if (yakin) {
            SessionManager.getInstance().logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) contentArea.getScene().getWindow();
                Scene scene = new Scene(root, 980, 640);
                stage.setTitle("MuseumMate — Masuk");
                stage.setScene(scene);
                stage.centerOnScreen();
            } catch (IOException e) {
                AlertHelper.showError("Kesalahan", "Gagal kembali ke layar login: " + e.getMessage());
            }
        }
    }

    private void setActiveNav(Button btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        activeButton = btn;
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            AlertHelper.showError("Pemuatan Halaman", "Gagal memuat tampilan: " + fxmlPath + "\n\nPesan: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
