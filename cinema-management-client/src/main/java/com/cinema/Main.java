package com.cinema;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML file
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/views/customer/customer-home.fxml"));

            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/views/cinema/seat-selection.fxml"));
            // FXMLLoader loader = new
            // FXMLLoader(getClass().getResource("/views/cinema/combo-selection.fxml"));

            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/booking-confirmation.fxml"));
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/booking-success.fxml"));
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/MainAdmin.fxml"));

                        
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/login.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root);

            // Setup stage
            primaryStage.setTitle("Cinema Pro - Hệ thống đặt vé");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            // FULL MÀN HÌNH + KHÔNG CHO RESIZE + KHÔNG CÓ VIỀN (đẹp như app thật)
            // primaryStage.setFullScreen(true); // Full màn hình
            primaryStage.setFullScreenExitHint(""); // Ẩn dòng "Press ESC to exit full screen"

            primaryStage.show();

            System.out.println("Application started successfully!");

        } catch (Exception e) {
            System.err.println("Error loading application:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}