package com.cinema.controllers.auth;

import com.cinema.utils.ApiClient;
import com.cinema.utils.AuthResponse;
import com.cinema.utils.UserInfo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;  // Đảm bảo trong FXML button có fx:id="loginButton"

    // Regex kiểm tra email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,6})$"
    );

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        passwordField.setOnAction(e -> handleLogin(null));
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Reset lỗi cũ
        errorLabel.setVisible(false);

        // Validate
        if (email.isBlank() || password.isBlank()) {
            showError("Vui lòng nhập email và mật khẩu");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Email không hợp lệ");
            return;
        }

        // === TRẠNG THÁI ĐANG ĐĂNG NHẬP ===
        loginButton.setDisable(true);
        loginButton.setText("Đang đăng nhập...");
        errorLabel.setText("Đang kết nối đến server...");
        errorLabel.setStyle("-fx-text-fill: #888899;");
        errorLabel.setVisible(true);

        // === GỌI API TRONG BACKGROUND THREAD ===
        new Thread(() -> {
            AuthResponse response = ApiClient.login(email, password);

            // Quay lại JavaFX Thread để cập nhật UI
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                loginButton.setText("ĐĂNG NHẬP");

                if (response.success) {
                    // Đăng nhập thành công
                    UserInfo user = ApiClient.getSavedUser();

                    String dashboardPath = switch (user.role) {
                        case "ADMIN" -> "/views/admin/dashboard.fxml";
                        case "CINEMA_MANAGER" -> "/views/manager/dashboard.fxml";
                        case "CUSTOMER" -> "/views/customer/customer-home.fxml";
                        default -> "/views/customer/customer-home.fxml"; // mặc định
                    };

                    navigateTo(dashboardPath);
                } else {
                    // Lỗi từ server hoặc kết nối
                    showError(response.message);
                }
            });
        }).start();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        showError("Tính năng quên mật khẩu đang phát triển.");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        navigateTo("/views/auth/register.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = stage.getScene();
            boolean isFullScreen = stage.isFullScreen();

            scene.setRoot(newRoot);

            if (isFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể mở trang: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ff4466;");
        errorLabel.setVisible(true);
    }
}