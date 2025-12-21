package com.cinema.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainAdminController {

    @FXML
    private AnchorPane contentArea;

    // ====================== LOAD NỘI DUNG CHÍNH - ĐÃ SỬA ======================
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load(); // để FXML tự load controller

            contentArea.getChildren().setAll(content);

            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không tải được trang: " + fxmlPath + "\nLỗi: " + e.getMessage());
        }
    }

    // ====================== MENU SIDEBAR ======================
    @FXML
    private void loadDashboard() {
        loadContent("/views/admin/partials/dashboard-content.fxml");
    }

    @FXML
    private void loadMovieList() {
        loadContent("/views/admin/partials/movie/movie-list-content.fxml");
    }

    @FXML
    private void loadCinemaList() {
        loadContent("/views/admin/partials/cinema/cinema-list-content.fxml");
    }

    @FXML
    private void loadSchedule() {
        loadContent("/views/admin/partials/schedule/schedule-calendar-content.fxml");
    }

    @FXML
    private void loadReport() {
        loadContent("/views/admin/partials/report/report-revenue-content.fxml");
    }

    @FXML
    private void loadBooking() {
        loadContent("/views/admin/partials/booking/booking-list-content.fxml");
    }

    @FXML
    private void loadFnb() {
        loadContent("/views/admin/partials/fnb/fnb-menu-content.fxml");
    }

    // ====================== MỞ MODAL ======================
    @FXML
    public void openMovieForm() {
        openModal("/views/admin/partials/movie/movie-form-content.fxml", "Thêm / Sửa Phim", 650, 700);
    }

    @FXML
    public void openCinemaForm() {
        openModal("/views/admin/partials/cinema/cinema-form-content.fxml", "Thêm / Sửa Rạp", 600, 500);
    }

    @FXML
    public void openBookingDetail() {
        openModal("/views/admin/partials/booking/booking-detail-content.fxml", "Chi Tiết Đơn Hàng", 700, 600);
    }

    @FXML
    public void openFnbForm() {
        openModal("/views/admin/partials/fnb/fnb-form-content.fxml", "Thêm / Sửa Món F&B", 600, 600);
    }

    // ====================== HÀM MỞ MODAL CHUNG ======================
    private void openModal(String fxmlPath, String title, double width, double height) {
        try {
            Parent modalRoot = FXMLLoader.load(getClass().getResource(fxmlPath));

            StackPane overlay = new StackPane(modalRoot);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

            // ✅ CHỈ GIỮ EFFECT, XÓA BACKGROUND WHITE:
            modalRoot.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 8);");

            Scene scene = new Scene(overlay, width, height);
            Stage modalStage = new Stage();
            modalStage.setTitle(title);
            modalStage.setScene(scene);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.initOwner(contentArea.getScene().getWindow());
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không mở được form:\n" + fxmlPath + "\nLỗi: " + e.getMessage());
        }
    }

    // ====================== ĐÓNG MODAL ======================
    @FXML
    public void closeModal() {
        javafx.stage.Window.getWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .filter(s -> s.getModality() == Modality.WINDOW_MODAL)
                .findFirst()
                .ifPresent(Stage::close);
    }

    // ====================== THÔNG BÁO LỖI ======================
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi Hệ Thống");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}