package com.museummate.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilitas modal dialog modern dan terstandarisasi untuk MuseumMate.
 * Mengadopsi desain modal card floating dengan concentric glowing badge,
 * tipografi centered yang bersih, dan tombol aksi terstruktur sesuai UI modern.
 */
public class AlertHelper {

    public enum DialogType {
        INFO, WARNING, ERROR, CONFIRMATION
    }

    public static void showInfo(String title, String message) {
        showCustomDialog(DialogType.INFO, title, null, message);
    }

    public static void showWarning(String title, String message) {
        showCustomDialog(DialogType.WARNING, title, null, message);
    }

    public static void showError(String title, String message) {
        showCustomDialog(DialogType.ERROR, title, null, message);
    }

    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(title, null, message);
    }

    public static boolean showConfirmation(String title, String header, String content) {
        return showCustomDialog(DialogType.CONFIRMATION, title, header, content);
    }

    private static boolean showCustomDialog(DialogType type, String title, String header, String message) {
        if (!Platform.isFxApplicationThread()) {
            AtomicBoolean res = new AtomicBoolean(false);
            try {
                java.util.concurrent.FutureTask<Boolean> task = new java.util.concurrent.FutureTask<>(
                        () -> showCustomDialog(type, title, header, message)
                );
                Platform.runLater(task);
                return task.get();
            } catch (Exception e) {
                return false;
            }
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        // Find active window for centering and ownership
        Window owner = null;
        for (Window w : Window.getWindows()) {
            if (w.isShowing()) {
                owner = w;
                break;
            }
        }
        if (owner != null) {
            stage.initOwner(owner);
        }

        boolean isDelete = false;
        if (type == DialogType.CONFIRMATION) {
            isDelete = (title != null && title.toLowerCase().contains("hapus")) ||
                       (header != null && header.toLowerCase().contains("hapus")) ||
                       (message != null && message.toLowerCase().contains("hapus"));
        }

        // Concentric glow ring colors & symbols based on dialog type
        String outerColor;
        String midColor;
        String solidColor;
        String iconChar;
        String primaryBtnColor;
        String primaryBtnHover;

        switch (type) {
            case INFO -> {
                boolean isSuccess = (title != null && (title.toLowerCase().contains("sukses") || title.toLowerCase().contains("berhasil"))) ||
                                    (header != null && (header.toLowerCase().contains("sukses") || header.toLowerCase().contains("berhasil")));
                if (isSuccess) {
                    outerColor = "rgba(16, 185, 129, 0.15)"; // emerald
                    midColor = "rgba(16, 185, 129, 0.28)";
                    solidColor = "#10b981";
                    iconChar = "✓";
                    primaryBtnColor = "#10b981";
                    primaryBtnHover = "#059669";
                } else {
                    outerColor = "rgba(37, 99, 235, 0.12)"; // blue
                    midColor = "rgba(37, 99, 235, 0.24)";
                    solidColor = "#2563eb";
                    iconChar = "i";
                    primaryBtnColor = "#2563eb";
                    primaryBtnHover = "#1d4ed8";
                }
            }
            case WARNING -> {
                outerColor = "rgba(245, 158, 11, 0.15)"; // amber
                midColor = "rgba(245, 158, 11, 0.28)";
                solidColor = "#f59e0b";
                iconChar = "!";
                primaryBtnColor = "#f59e0b";
                primaryBtnHover = "#d97706";
            }
            case ERROR -> {
                outerColor = "rgba(239, 68, 68, 0.15)"; // red
                midColor = "rgba(239, 68, 68, 0.28)";
                solidColor = "#ef4444";
                iconChar = "!";
                primaryBtnColor = "#ef4444";
                primaryBtnHover = "#dc2626";
            }
            case CONFIRMATION -> {
                if (isDelete) {
                    outerColor = "rgba(239, 68, 68, 0.15)"; // red
                    midColor = "rgba(239, 68, 68, 0.28)";
                    solidColor = "#ef4444";
                    iconChar = "!";
                    primaryBtnColor = "#dc2626";
                    primaryBtnHover = "#b91c1c";
                } else {
                    outerColor = "rgba(37, 99, 235, 0.12)"; // blue
                    midColor = "rgba(37, 99, 235, 0.24)";
                    solidColor = "#2563eb";
                    iconChar = "?";
                    primaryBtnColor = "#2563eb";
                    primaryBtnHover = "#1d4ed8";
                }
            }
            default -> {
                outerColor = "rgba(37, 99, 235, 0.12)";
                midColor = "rgba(37, 99, 235, 0.24)";
                solidColor = "#2563eb";
                iconChar = "i";
                primaryBtnColor = "#2563eb";
                primaryBtnHover = "#1d4ed8";
            }
        }

        // 1. Concentric Glowing Badge
        Label lblSymbol = new Label(iconChar);
        lblSymbol.setStyle("-fx-font-size: 13px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");

        StackPane innerCircle = new StackPane(lblSymbol);
        innerCircle.setMinSize(28, 28);
        innerCircle.setMaxSize(28, 28);
        innerCircle.setStyle("-fx-background-color: " + solidColor + "; -fx-background-radius: 14px;");

        StackPane midCircle = new StackPane(innerCircle);
        midCircle.setMinSize(40, 40);
        midCircle.setMaxSize(40, 40);
        midCircle.setStyle("-fx-background-color: " + midColor + "; -fx-background-radius: 20px;");

        StackPane outerCircle = new StackPane(midCircle);
        outerCircle.setMinSize(52, 52);
        outerCircle.setMaxSize(52, 52);
        outerCircle.setStyle("-fx-background-color: " + outerColor + "; -fx-background-radius: 26px;");

        // 2. Top Bar: Centered Badge + Top-Right Close Button
        final AtomicBoolean confirmed = new AtomicBoolean(false);

        Button btnClose = new Button("\u2715");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 3px 6px; -fx-background-radius: 6px;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 3px 6px; -fx-background-radius: 6px;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 3px 6px; -fx-background-radius: 6px;"));
        btnClose.setOnAction(e -> {
            confirmed.set(false);
            stage.close();
        });

        StackPane topBar = new StackPane(outerCircle, btnClose);
        StackPane.setAlignment(outerCircle, Pos.CENTER);
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);
        StackPane.setMargin(btnClose, new Insets(-6, -10, 0, 0));

        // 3. Text Group (Centered Title & Description)
        VBox textGroup = new VBox(6);
        textGroup.setAlignment(Pos.CENTER);

        String mainTitle = (header != null && !header.isBlank()) ? header : (title != null ? title : "Pemberitahuan");
        Label lblTitle = new Label(mainTitle);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #0f172a; -fx-text-alignment: center;");
        lblTitle.setWrapText(true);
        lblTitle.setMaxWidth(340);
        lblTitle.setAlignment(Pos.CENTER);
        textGroup.getChildren().add(lblTitle);

        if (message != null && !message.isBlank()) {
            Label lblMessage = new Label(message);
            lblMessage.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #64748b; -fx-text-alignment: center; -fx-line-spacing: 2.5px;");
            lblMessage.setWrapText(true);
            lblMessage.setMaxWidth(340);
            lblMessage.setAlignment(Pos.CENTER);
            textGroup.getChildren().add(lblMessage);
        }

        // 4. Action Buttons
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        String cancelNormal = "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand;";
        String cancelHover = "-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-text-fill: #1f2937; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand;";

        String primaryNormal = "-fx-background-color: " + primaryBtnColor + "; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 12px; -fx-cursor: hand;";
        String primaryHover = "-fx-background-color: " + primaryBtnHover + "; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 12px; -fx-cursor: hand;";

        if (type == DialogType.CONFIRMATION) {
            Button btnCancel = new Button("Batal");
            btnCancel.setMinWidth(135);
            btnCancel.setPrefWidth(145);
            btnCancel.setMinHeight(42);
            setupHover(btnCancel, cancelNormal, cancelHover);
            btnCancel.setOnAction(e -> {
                confirmed.set(false);
                stage.close();
            });

            String confirmText = isDelete ? "Ya, Hapus" : "Konfirmasi";
            Button btnConfirm = new Button(confirmText);
            btnConfirm.setMinWidth(135);
            btnConfirm.setPrefWidth(145);
            btnConfirm.setMinHeight(42);
            setupHover(btnConfirm, primaryNormal, primaryHover);
            btnConfirm.setOnAction(e -> {
                confirmed.set(true);
                stage.close();
            });

            // If Delete: [ Delete (Red) ] [ Cancel (Outline) ] exactly like top modal in reference
            // If Other:  [ Cancel (Outline) ] [ Confirm (Blue) ] exactly like bottom modal in reference
            if (isDelete) {
                buttonBar.getChildren().addAll(btnConfirm, btnCancel);
            } else {
                buttonBar.getChildren().addAll(btnCancel, btnConfirm);
            }
        } else {
            Button btnOk = new Button("Mengerti");
            btnOk.setMinWidth(150);
            btnOk.setPrefWidth(165);
            btnOk.setMinHeight(42);
            setupHover(btnOk, primaryNormal, primaryHover);
            btnOk.setOnAction(e -> stage.close());
            buttonBar.getChildren().add(btnOk);
        }

        // 5. Inner Card Container
        VBox innerCard = new VBox(14);
        innerCard.setAlignment(Pos.CENTER);
        innerCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 20px; -fx-border-color: #f1f5f9; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 22px 28px 26px 28px;");
        innerCard.setMinWidth(380);
        innerCard.setMaxWidth(410);
        innerCard.getChildren().addAll(topBar, textGroup, buttonBar);

        // 6. Outer Translucent Rounded Frame (matching the double-pill / soft outer rim in reference image)
        StackPane outerFrame = new StackPane(innerCard);
        outerFrame.setStyle("-fx-background-color: rgba(255, 255, 255, 0.75); -fx-background-radius: 28px; -fx-padding: 8px;");

        // Soft elevated Drop Shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(32);
        shadow.setSpread(0.04);
        shadow.setColor(Color.rgb(15, 23, 42, 0.16));
        shadow.setOffsetY(10);
        outerFrame.setEffect(shadow);

        // Allow dragging dialog window smoothly
        final double[] dragDelta = new double[2];
        outerFrame.setOnMousePressed(mouseEvent -> {
            dragDelta[0] = stage.getX() - mouseEvent.getScreenX();
            dragDelta[1] = stage.getY() - mouseEvent.getScreenY();
        });
        outerFrame.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta[0]);
            stage.setY(mouseEvent.getScreenY() + dragDelta[1]);
        });

        // Root container with transparent padding for drop shadow
        StackPane root = new StackPane(outerFrame);
        root.setStyle("-fx-background-color: transparent; -fx-padding: 24px;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // Keyboard handler (ESC to cancel, ENTER to confirm)
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                confirmed.set(false);
                stage.close();
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
                if (type == DialogType.CONFIRMATION) {
                    confirmed.set(true);
                }
                stage.close();
            }
        });

        stage.setScene(scene);

        // Center smoothly on parent window
        final Window parentWindow = owner;
        if (parentWindow != null) {
            stage.setOnShown(e -> {
                stage.setX(parentWindow.getX() + (parentWindow.getWidth() - stage.getWidth()) / 2.0);
                stage.setY(parentWindow.getY() + (parentWindow.getHeight() - stage.getHeight()) / 2.0);
            });
        }

        stage.showAndWait();
        return confirmed.get();
    }

    private static void setupHover(Button btn, String normalStyle, String hoverStyle) {
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
    }
}
