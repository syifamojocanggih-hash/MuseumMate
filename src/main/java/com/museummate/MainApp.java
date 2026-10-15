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

            Scene scene = new Scene(root, 920, 620);
            primaryStage.setTitle("MuseumMate — Museum Surabaya (Gedung Siola)");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(550);
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
