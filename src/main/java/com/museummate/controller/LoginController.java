package com.museummate.controller;

import com.museummate.model.Pengguna;
import com.museummate.model.PeranPengguna;
import com.museummate.service.AuthService;
import com.museummate.util.AlertHelper;
import com.museummate.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller untuk halaman autentikasi dan login (PRD F1).
 * Mendukung switch role Admin vs Konservator, pemilihan spesialisasi kurasi
 * (Artefak, Naskah, Lukisan, Fosil), dan akun demo cepat untuk masing-masing bidang.
 */
public class LoginController {

    @FXML private Label lblLoginTitle;
    @FXML private Label lblLoginSubtitle;

    @FXML private Button btnRoleAdmin;
    @FXML private Button btnRoleKonservator;

    @FXML private VBox boxSpesialisasiKonservator;
    @FXML private Button btnSpecArtefak;
    @FXML private Button btnSpecNaskah;
    @FXML private Button btnSpecLukisan;
    @FXML private Button btnSpecFosil;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordPlain;
    @FXML private Button btnTogglePassword;
    @FXML private HBox boxUsername;
    @FXML private HBox boxPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    @FXML private VBox boxDemoAdmin;
    @FXML private VBox boxDemoKonservator;

    @FXML private StackPane visualPane;
    @FXML private ImageView imgVisual;

    private final AuthService authService = new AuthService();
    private String selectedRole = "ADMIN";
    private String selectedSpec = "ARTEFAK";

    @FXML
    public void initialize() {
        // Sync password text between masked and unmasked fields
        if (txtPasswordPlain != null && txtPassword != null) {
            txtPasswordPlain.textProperty().bindBidirectional(txtPassword.textProperty());
        }

        // Enter key flow: Username -> Password -> Submit
        txtUsername.setOnAction(e -> {
            if (txtPassword.isVisible()) {
                txtPassword.requestFocus();
            } else if (txtPasswordPlain != null) {
                txtPasswordPlain.requestFocus();
            }
        });

        txtPassword.setOnAction(e -> handleLogin());
        if (txtPasswordPlain != null) {
            txtPasswordPlain.setOnAction(e -> handleLogin());
        }

        // Focus styling on container boxes
        if (boxUsername != null) {
            txtUsername.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) boxUsername.getStyleClass().add("focused");
                else boxUsername.getStyleClass().remove("focused");
            });
        }

        if (boxPassword != null) {
            txtPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) boxPassword.getStyleClass().add("focused");
                else boxPassword.getStyleClass().remove("focused");
            });

            if (txtPasswordPlain != null) {
                txtPasswordPlain.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) boxPassword.getStyleClass().add("focused");
                    else boxPassword.getStyleClass().remove("focused");
                });
            }
        }

        // Dynamic visual panel image sizing
        if (imgVisual != null && visualPane != null) {
            imgVisual.fitWidthProperty().bind(visualPane.widthProperty());
            imgVisual.fitHeightProperty().bind(visualPane.heightProperty());
        }

        // Default: Admin mode
        applyRoleMode("ADMIN");
        fillAdminDemo();
    }

    @FXML
    public void handleSelectRoleAdmin() {
        applyRoleMode("ADMIN");
        fillAdminDemo();
    }

    @FXML
    public void handleSelectRoleKonservator() {
        applyRoleMode("KONSERVATOR");
        applySpecSelection(selectedSpec);
    }

    private void applyRoleMode(String role) {
        this.selectedRole = role;
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (btnRoleAdmin != null && btnRoleKonservator != null) {
            if (isAdmin) {
                btnRoleAdmin.setStyle("-fx-background-color: #174C78; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8px 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
                btnRoleKonservator.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8px 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
            } else {
                btnRoleAdmin.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8px 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
                btnRoleKonservator.setStyle("-fx-background-color: #047857; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8px 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
            }
        }

        if (boxSpesialisasiKonservator != null) {
            boxSpesialisasiKonservator.setVisible(!isAdmin);
            boxSpesialisasiKonservator.setManaged(!isAdmin);
        }

        if (boxDemoAdmin != null) {
            boxDemoAdmin.setVisible(isAdmin);
            boxDemoAdmin.setManaged(isAdmin);
        }

        if (boxDemoKonservator != null) {
            boxDemoKonservator.setVisible(!isAdmin);
            boxDemoKonservator.setManaged(!isAdmin);
        }

        if (lblLoginTitle != null) {
            lblLoginTitle.setText(isAdmin ? "Masuk sebagai Administrator" : "Masuk sebagai Konservator");
        }
        if (lblLoginSubtitle != null) {
            lblLoginSubtitle.setText(isAdmin
                    ? "Akses kontrol operasional museum, manajemen pengguna & log audit"
                    : "Kelola kondisi fisik, jadwal perawatan, dan dokumentasi foto koleksi");
        }
    }

    @FXML
    public void handleSelectSpecArtefak() {
        applySpecSelection("ARTEFAK");
        fillDemoArtefak();
    }

    @FXML
    public void handleSelectSpecNaskah() {
        applySpecSelection("NASKAH");
        fillDemoNaskah();
    }

    @FXML
    public void handleSelectSpecLukisan() {
        applySpecSelection("LUKISAN");
        fillDemoLukisan();
    }

    @FXML
    public void handleSelectSpecFosil() {
        applySpecSelection("FOSIL");
        fillDemoFosil();
    }

    private void applySpecSelection(String spec) {
        this.selectedSpec = spec != null ? spec.toUpperCase() : "ARTEFAK";

        setSpecBtnState(btnSpecArtefak, "ARTEFAK".equals(this.selectedSpec));
        setSpecBtnState(btnSpecNaskah, "NASKAH".equals(this.selectedSpec));
        setSpecBtnState(btnSpecLukisan, "LUKISAN".equals(this.selectedSpec));
        setSpecBtnState(btnSpecFosil, "FOSIL".equals(this.selectedSpec));
    }

    private void setSpecBtnState(Button btn, boolean active) {
        if (btn == null) return;
        btn.getStyleClass().removeAll("spec-chip-btn", "spec-chip-btn-active");
        if (active) {
            btn.getStyleClass().add("spec-chip-btn-active");
        } else {
            btn.getStyleClass().add("spec-chip-btn");
        }
    }

    @FXML
    public void togglePasswordVisibility() {
        if (txtPassword == null || txtPasswordPlain == null) return;

        boolean isMasked = txtPassword.isVisible();
        if (isMasked) {
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            txtPasswordPlain.setVisible(true);
            txtPasswordPlain.setManaged(true);
            txtPasswordPlain.requestFocus();
            txtPasswordPlain.positionCaret(txtPasswordPlain.getText().length());
            btnTogglePassword.setText("🙈");
        } else {
            txtPasswordPlain.setVisible(false);
            txtPasswordPlain.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPassword.requestFocus();
            txtPassword.positionCaret(txtPassword.getText().length());
            btnTogglePassword.setText("👁");
        }
    }

    @FXML
    public void handleLogin() {
        lblError.setVisible(false);
        lblError.setManaged(false);

        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {
            Pengguna user = authService.login(username, password);

            // Jika login sebagai konservator, pastikan spesialisasi diselaraskan
            if (user.getPeran() == PeranPengguna.KONSERVATOR) {
                if (user.getSpesialisasi() == null || "SEMUA".equalsIgnoreCase(user.getSpesialisasi())) {
                    user.setSpesialisasi(selectedSpec);
                    SessionManager.getInstance().setSessionSpesialisasi(selectedSpec);
                }
            }

            bukaMainLayout(user);
        } catch (IllegalArgumentException e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
            lblError.setManaged(true);
        } catch (Exception e) {
            AlertHelper.showError("Koneksi Basis Data",
                    "Tidak dapat terhubung ke MySQL.\nPastikan MySQL di Laragon / XAMPP sudah berjalan (port 3306).\n\nPesan teknis: " + e.getMessage());
        }
    }

    @FXML
    public void fillAdminDemo() {
        txtUsername.setText("admin");
        txtPassword.setText("admin123");
        focusPassword();
    }

    @FXML
    public void fillKonservatorDemo() {
        fillDemoArtefak();
    }

    @FXML
    public void fillDemoArtefak() {
        applyRoleMode("KONSERVATOR");
        applySpecSelection("ARTEFAK");
        txtUsername.setText("konservator_artefak");
        txtPassword.setText("konservator123");
        focusPassword();
    }

    @FXML
    public void fillDemoNaskah() {
        applyRoleMode("KONSERVATOR");
        applySpecSelection("NASKAH");
        txtUsername.setText("konservator_naskah");
        txtPassword.setText("konservator123");
        focusPassword();
    }

    @FXML
    public void fillDemoLukisan() {
        applyRoleMode("KONSERVATOR");
        applySpecSelection("LUKISAN");
        txtUsername.setText("konservator_lukisan");
        txtPassword.setText("konservator123");
        focusPassword();
    }

    @FXML
    public void fillDemoFosil() {
        applyRoleMode("KONSERVATOR");
        applySpecSelection("FOSIL");
        txtUsername.setText("konservator_fosil");
        txtPassword.setText("konservator123");
        focusPassword();
    }

    private void focusPassword() {
        if (txtPassword.isVisible()) {
            txtPassword.requestFocus();
        } else if (txtPasswordPlain != null) {
            txtPasswordPlain.requestFocus();
        }
    }

    private void bukaMainLayout(Pengguna user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/main_layout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 780);
            stage.setTitle("MuseumMate — Sistem Manajemen Koleksi & Konservasi Museum");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertHelper.showError("Kesalahan Pemuatan", "Gagal memuat antarmuka utama: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
