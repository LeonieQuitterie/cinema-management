package com.cinema.controllers.auth;

import com.cinema.utils.ApiClient;
import com.cinema.utils.AuthResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button registerButton;  // Đảm bảo trong FXML có fx:id="registerButton"

    @FXML
    private void initialize() {
        messageLabel.setVisible(false);
        confirmPasswordField.setOnAction(e -> handleRegister(null));
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Reset thông báo cũ
        messageLabel.setVisible(false);

        // Validate cơ bản
        if (fullName.isBlank() || username.isBlank() || email.isBlank() ||
            password.isBlank() || confirmPassword.isBlank()) {
            showMessage("Vui lòng nhập đầy đủ thông tin", true);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showMessage("Email không hợp lệ", true);
            return;
        }

        if (username.length() < 4) {
            showMessage("Tên đăng nhập phải có ít nhất 4 ký tự", true);
            return;
        }

        if (password.length() < 6) {
            showMessage("Mật khẩu phải có ít nhất 6 ký tự", true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Mật khẩu nhập lại không khớp", true);
            return;
        }

        // === TRẠNG THÁI ĐANG ĐĂNG KÝ ===
        registerButton.setDisable(true);
        registerButton.setText("Đang tạo tài khoản...");
        showMessage("Đang kết nối đến server...", false);

        // === GỌI API TRONG BACKGROUND THREAD ===
        new Thread(() -> {
            AuthResponse response = ApiClient.register(fullName, username, email, password, confirmPassword);

            // Quay lại JavaFX Thread để cập nhật UI
            Platform.runLater(() -> {
                registerButton.setDisable(false);
                registerButton.setText("TẠO TÀI KHOẢN");

                if (response.success) {
                    showMessage("Đăng ký thành công! Chào mừng bạn đến với Cinema Pro 🎉", false);
                    // Chuyển về trang đăng nhập sau 1.5 giây
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1500);
                            navigateTo("/views/auth/login.fxml");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                } else {
                    // Hiển thị lỗi chính xác từ server
                    showMessage(response.message, true);
                }
            });
        }).start();
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        navigateTo("/views/auth/login.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = stage.getScene();
            boolean fullScreen = stage.isFullScreen();

            scene.setRoot(root);

            if (fullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Không thể chuyển trang: " + e.getMessage(), true);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError
                ? "-fx-text-fill: #ff4466;"     // đỏ - lỗi
                : "-fx-text-fill: #44ff99;");   // xanh lá - thành công
        messageLabel.setVisible(true);
    }
}