package com.museummate.controller;

import com.museummate.model.JadwalPerawatan;
import com.museummate.model.Pengguna;
import com.museummate.util.AlertHelper;
import com.museummate.util.DateUtil;
import com.museummate.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller utama pengatur tata letak (Sidebar & Navigasi Antar Halaman).
 * Menyesuaikan visibilitas menu sesuai peran pengguna aktif (PRD F1).
 */
public class MainLayoutController {

    @FXML private Label lblNamaPengguna;
    @FXML private Label lblPeranPengguna;

    @FXML private TextField txtGlobalSearch;
    @FXML private Label lblTanggalHariIni;
    @FXML private Button btnTopNotifikasi;
    @FXML private Label lblAvatarInitials;
    @FXML private Label lblHeaderUserName;
    @FXML private Label lblHeaderUserRole;

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
    private Popup notifPopup;

    @FXML
    public void initialize() {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblNamaPengguna.setText(user.getNamaLengkap());
            lblPeranPengguna.setText(user.getPeran().getLabel().toUpperCase());
            if (lblHeaderUserName != null) lblHeaderUserName.setText(user.getNamaLengkap());
            if (lblHeaderUserRole != null) lblHeaderUserRole.setText(user.getPeran().getLabel().toUpperCase());
            if (lblAvatarInitials != null) lblAvatarInitials.setText(hitungInisial(user.getNamaLengkap()));

            // Menu Pengguna dan Log Audit hanya untuk Admin (PRD F1 / F10)
            boolean isAdmin = user.isAdmin();
            btnNavPengguna.setVisible(isAdmin);
            btnNavPengguna.setManaged(isAdmin);

            btnNavLogAudit.setVisible(isAdmin);
            btnNavLogAudit.setManaged(isAdmin);
        }

        if (lblTanggalHariIni != null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale.forLanguageTag("id-ID"));
            lblTanggalHariIni.setText(now.format(dtf));
        }

        updateNotifikasiBadge();

        // Tampilkan Dashboard sebagai halaman awal
        showDashboard();
    }

    private String hitungInisial(String nama) {
        if (nama == null || nama.isBlank()) return "MM";
        String[] parts = nama.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private void updateNotifikasiBadge() {
        try {
            int notifCount = jadwalService.getJumlahNotifikasiAktif();
            if (notifCount > 0) {
                btnNavNotifikasi.setText("🔔  Notifikasi Jadwal (" + notifCount + ")");
                if (btnTopNotifikasi != null) {
                    btnTopNotifikasi.setGraphic(createBellWithBadge(notifCount));
                }
            } else {
                btnNavNotifikasi.setText("🔔  Notifikasi Jadwal");
                if (btnTopNotifikasi != null) {
                    Label bell = new Label("🔔");
                    bell.setStyle("-fx-font-size: 16px; -fx-text-fill: #334155;");
                    btnTopNotifikasi.setGraphic(bell);
                }
            }
        } catch (Exception e) {
            btnNavNotifikasi.setText("🔔  Notifikasi Jadwal");
            if (btnTopNotifikasi != null) {
                Label bell = new Label("🔔");
                bell.setStyle("-fx-font-size: 16px; -fx-text-fill: #334155;");
                btnTopNotifikasi.setGraphic(bell);
            }
        }
    }

    private Node createBellWithBadge(int count) {
        StackPane stack = new StackPane();
        Label bell = new Label("🔔");
        bell.setStyle("-fx-font-size: 16px; -fx-text-fill: #334155;");

        Label badge = new Label(count > 99 ? "99+" : String.valueOf(count));
        badge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1px 4px; -fx-background-radius: 8px;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(-4, -6, 0, 0));

        stack.getChildren().addAll(bell, badge);
        return stack;
    }

    @FXML
    public void handleTopNotifikasiClick() {
        if (notifPopup != null && notifPopup.isShowing()) {
            notifPopup.hide();
            return;
        }
        showNotificationPopup();
    }

    private void showNotificationPopup() {
        if (btnTopNotifikasi == null || btnTopNotifikasi.getScene() == null) return;

        notifPopup = new Popup();
        notifPopup.setAutoHide(true);

        VBox popupCard = new VBox(12);
        popupCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 14px; -fx-border-radius: 14px; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-padding: 16px;");
        popupCard.setMinWidth(380);
        popupCard.setMaxWidth(400);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setSpread(0.12);
        shadow.setColor(Color.rgb(0, 0, 0, 0.18));
        shadow.setOffsetY(8);
        popupCard.setEffect(shadow);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("🔔 Peringatan & Notifikasi");
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int totalCount = 0;
        List<JadwalPerawatan> listTerlambat = new ArrayList<>();
        List<JadwalPerawatan> listMendatang = new ArrayList<>();
        try {
            listTerlambat = jadwalService.getJadwalTerlambat();
            listMendatang = jadwalService.getJadwalMendatang(7);
            totalCount = listTerlambat.size() + listMendatang.size();
        } catch (Exception ignored) {}

        Label lblBadge = new Label(totalCount > 0 ? totalCount + " Jadwal Aktif" : "Aman");
        lblBadge.setStyle(totalCount > 0
                ? "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 10px;"
                : "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 10px;");

        header.getChildren().addAll(lblTitle, spacer, lblBadge);

        // List Container
        VBox listContainer = new VBox(8);

        List<JadwalPerawatan> combined = new ArrayList<>();
        combined.addAll(listTerlambat);
        combined.addAll(listMendatang);

        if (combined.isEmpty()) {
            VBox emptyBox = new VBox(6);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(16, 8, 16, 8));
            Label lblEmptyIcon = new Label("✅");
            lblEmptyIcon.setStyle("-fx-font-size: 24px;");
            Label lblEmptyText = new Label("Semua jadwal perawatan dalam kondisi aman.");
            lblEmptyText.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
            emptyBox.getChildren().addAll(lblEmptyIcon, lblEmptyText);
            listContainer.getChildren().add(emptyBox);
        } else {
            int maxItems = Math.min(3, combined.size());
            for (int i = 0; i < maxItems; i++) {
                JadwalPerawatan j = combined.get(i);
                VBox itemCard = new VBox(3);
                itemCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 9px 12px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-cursor: hand;");

                itemCard.setOnMouseEntered(ev -> itemCard.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 9px 12px; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-cursor: hand;"));
                itemCard.setOnMouseExited(ev -> itemCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 9px 12px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-cursor: hand;"));

                HBox rowTop = new HBox(6);
                rowTop.setAlignment(Pos.CENTER_LEFT);

                Label lblStatus = new Label(j.isTerlambat() ? "TERLAMBAT" : "JATUH TEMPO");
                lblStatus.setStyle(j.isTerlambat()
                        ? "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 4px;"
                        : "-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 4px;");

                Region rSpacer = new Region();
                HBox.setHgrow(rSpacer, Priority.ALWAYS);

                Label lblDate = new Label(j.getTanggalRencana() != null ? DateUtil.formatIndonesian(j.getTanggalRencana()) : "-");
                lblDate.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #174C78;");

                rowTop.getChildren().addAll(lblStatus, rSpacer, lblDate);

                Label lblKeg = new Label(j.getKegiatan());
                lblKeg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                Label lblKol = new Label(j.getNamaKoleksi() != null ? j.getNamaKoleksi() : "Koleksi ID " + j.getIdKoleksi());
                lblKol.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                itemCard.getChildren().addAll(rowTop, lblKeg, lblKol);

                // Clicking an item opens the notification detail
                itemCard.setOnMouseClicked(ev -> {
                    notifPopup.hide();
                    showNotifikasi();
                });

                listContainer.getChildren().add(itemCard);
            }
        }

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.5;");

        // Footer button: "Lihat Selengkapnya →"
        Button btnLihatSemua = new Button("Lihat Selengkapnya →");
        btnLihatSemua.setMaxWidth(Double.MAX_VALUE);
        btnLihatSemua.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #174C78; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 9px 0; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-cursor: hand;");
        btnLihatSemua.setOnMouseEntered(ev -> btnLihatSemua.setStyle("-fx-background-color: #174C78; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 9px 0; -fx-background-radius: 8px; -fx-border-color: #174C78; -fx-border-radius: 8px; -fx-cursor: hand;"));
        btnLihatSemua.setOnMouseExited(ev -> btnLihatSemua.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #174C78; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 9px 0; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-cursor: hand;"));
        btnLihatSemua.setOnAction(ev -> {
            notifPopup.hide();
            showNotifikasi();
        });

        popupCard.getChildren().addAll(header, listContainer, sep, btnLihatSemua);
        notifPopup.getContent().setAll(popupCard);

        Point2D p = btnTopNotifikasi.localToScreen(0, 0);
        if (p != null) {
            double x = p.getX() + btnTopNotifikasi.getWidth() - 380;
            double y = p.getY() + btnTopNotifikasi.getHeight() + 8;
            notifPopup.show(btnTopNotifikasi.getScene().getWindow(), x, y);
        }
    }

    @FXML
    public void handleGlobalSearch() {
        String query = txtGlobalSearch != null ? txtGlobalSearch.getText() : null;
        showKoleksi();
        if (query != null && !query.isBlank() && contentArea != null) {
            Node searchField = contentArea.lookup("#txtSearch");
            if (searchField instanceof TextField tf) {
                tf.setText(query);
            }
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
