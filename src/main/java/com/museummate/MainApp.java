package com.museummate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Titik masuk utama aplikasi desktop MuseumMate (JavaFX).
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/museummate/view/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 980, 640);
            primaryStage.setTitle("MuseumMate — Museum Surabaya (Gedung Siola)");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(880);
            primaryStage.setMinHeight(580);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Gagal memulai aplikasi MuseumMate: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
