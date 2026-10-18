package com.museummate.controller;

import com.museummate.model.Pengguna;
import com.museummate.service.AuthService;
import com.museummate.util.AlertHelper;
import javafx.application.Platform;
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
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller untuk halaman autentikasi dan login (PRD F1).
 * Mendukung layout modern split-screen, password toggle, dan quick-fill akun demo.
 */
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordPlain;
    @FXML private Button btnTogglePassword;
    @FXML private HBox boxUsername;
    @FXML private HBox boxPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;
    @FXML private StackPane visualPane;
    @FXML private ImageView imgVisual;

    private final AuthService authService = new AuthService();

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
        if (txtPassword.isVisible()) {
            txtPassword.requestFocus();
        } else if (txtPasswordPlain != null) {
            txtPasswordPlain.requestFocus();
        }
    }

    @FXML
    public void fillKonservatorDemo() {
        txtUsername.setText("konservator");
        txtPassword.setText("konservator123");
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
