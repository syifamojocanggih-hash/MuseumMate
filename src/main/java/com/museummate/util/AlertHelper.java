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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
 * Menghadirkan tampilan kartu modal elegan dengan ikon badge berwarna,
 * tipografi rapi, dan tombol aksi bergaya kontemporer.
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

        // Palette & badge icons based on dialog type
        String iconChar;
        String iconBg;
        String iconColor;
        String primaryBtnText = "Mengerti";
        String primaryBtnColor = "#174C78"; // Royal Navy

        switch (type) {
            case INFO -> {
                iconChar = "✓";
                iconBg = "#dcfce7";   // soft emerald green
                iconColor = "#16a34a";
            }
            case WARNING -> {
                iconChar = "⚠";
                iconBg = "#fef3c7";   // soft amber
                iconColor = "#d97706";
            }
            case ERROR -> {
                iconChar = "✕";
                iconBg = "#fee2e2";   // soft red
                iconColor = "#dc2626";
                primaryBtnColor = "#dc2626";
            }
            case CONFIRMATION -> {
                boolean isDelete = (title != null && title.toLowerCase().contains("hapus")) ||
                                   (header != null && header.toLowerCase().contains("hapus"));
                if (isDelete) {
                    iconChar = "🗑";
                    iconBg = "#fee2e2";
                    iconColor = "#dc2626";
                    primaryBtnText = "Ya, Hapus";
                    primaryBtnColor = "#dc2626";
                } else {
                    iconChar = "?";
                    iconBg = "#e0f2fe";
                    iconColor = "#0284c7";
                    primaryBtnText = "Ya, Lanjutkan";
                    primaryBtnColor = "#174C78";
                }
            }
            default -> {
                iconChar = "i";
                iconBg = "#e0f2fe";
                iconColor = "#0284c7";
            }
        }

        // Circular Icon Badge
        Label lblIcon = new Label(iconChar);
        lblIcon.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + iconColor + ";");
        StackPane iconCircle = new StackPane(lblIcon);
        iconCircle.setMinSize(44, 44);
        iconCircle.setMaxSize(44, 44);
        iconCircle.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 22px;");

        // Close '✕' Button
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4px 8px; -fx-background-radius: 6px;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4px 8px; -fx-background-radius: 6px;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4px 8px; -fx-background-radius: 6px;"));
        btnClose.setOnAction(e -> stage.close());

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(iconCircle, topSpacer, btnClose);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Text Group
        VBox textGroup = new VBox(6);
        Label lblTitle = new Label(title != null ? title : "Informasi");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        textGroup.getChildren().add(lblTitle);

        if (header != null && !header.isBlank()) {
            Label lblHeader = new Label(header);
            lblHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #334155;");
            lblHeader.setWrapText(true);
            lblHeader.setMaxWidth(380);
            textGroup.getChildren().add(lblHeader);
        }

        if (message != null && !message.isBlank()) {
            Label lblMsg = new Label(message);
            lblMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-line-spacing: 3px;");
            lblMsg.setWrapText(true);
            lblMsg.setMaxWidth(380);
            textGroup.getChildren().add(lblMsg);
        }

        // Action Buttons
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        AtomicBoolean confirmed = new AtomicBoolean(false);

        if (type == DialogType.CONFIRMATION) {
            Button btnCancel = new Button("Batal");
            btnCancel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: 600; -fx-font-size: 12.5px; -fx-padding: 9px 18px; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-cursor: hand;");
            btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; -fx-font-weight: 600; -fx-font-size: 12.5px; -fx-padding: 9px 18px; -fx-background-radius: 8px; -fx-border-color: #94a3b8; -fx-border-radius: 8px; -fx-cursor: hand;"));
            btnCancel.setOnMouseExited(e -> btnCancel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: 600; -fx-font-size: 12.5px; -fx-padding: 9px 18px; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-cursor: hand;"));
            btnCancel.setOnAction(e -> {
                confirmed.set(false);
                stage.close();
            });

            Button btnConfirm = new Button(primaryBtnText);
            btnConfirm.setStyle("-fx-background-color: " + primaryBtnColor + "; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 9px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");
            btnConfirm.setOnAction(e -> {
                confirmed.set(true);
                stage.close();
            });

            buttonBar.getChildren().addAll(btnCancel, btnConfirm);
        } else {
            Button btnOk = new Button("Mengerti");
            btnOk.setStyle("-fx-background-color: " + primaryBtnColor + "; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 9px 24px; -fx-background-radius: 8px; -fx-cursor: hand;");
            btnOk.setOnAction(e -> stage.close());
            buttonBar.getChildren().add(btnOk);
        }

        // Card Container
        VBox card = new VBox(14);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16px; -fx-border-radius: 16px; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-padding: 24px;");
        card.setMinWidth(420);
        card.setMaxWidth(440);
        card.getChildren().addAll(topBar, textGroup, buttonBar);

        // Soft elevated Drop Shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(24);
        shadow.setSpread(0.12);
        shadow.setColor(Color.rgb(0, 0, 0, 0.22));
        shadow.setOffsetY(8);
        card.setEffect(shadow);

        // Allow dragging dialog window smoothly
        final double[] dragDelta = new double[2];
        card.setOnMousePressed(mouseEvent -> {
            dragDelta[0] = stage.getX() - mouseEvent.getScreenX();
            dragDelta[1] = stage.getY() - mouseEvent.getScreenY();
        });
        card.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta[0]);
            stage.setY(mouseEvent.getScreenY() + dragDelta[1]);
        });

        // Root container with transparent padding for drop shadow
        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: transparent; -fx-padding: 20px;");
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
}
