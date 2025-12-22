package com.cinema.controllers.admin.cinema;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

import com.cinema.models.Cinema;
import com.cinema.utils.admin.CinemaApi;

public class CinemaFormController implements Initializable {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> cityCombo;
    @FXML private TextField addressField;
    // @FXML private TextField phoneField;
    @FXML private TextField logoUrlField;
    @FXML private ImageView logoPreview;
    @FXML private Label validationLabel;
    @FXML private Button saveButton;

    private Cinema cinema; // Null = create, Not null = edit
    // private File selectedLogoFile;
    private Runnable onSaveCallback;
    private CinemaApi cinemaApi;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cinemaApi = new CinemaApi();

        // Setup city combo
        cityCombo.getItems().addAll("Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", "Hải Phòng");

        // Hide validation initially
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);

        // Load logo when URL changes
        logoUrlField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                loadImageFromUrl(newVal.trim());
            } else {
                logoPreview.setImage(null);
            }
        });
    }

    private void loadImageFromUrl(String url) {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                );
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (InputStream is = conn.getInputStream()) {
                    Image image = new Image(is);
                    Platform.runLater(() -> logoPreview.setImage(image));
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("❌ Invalid image URL: " + url);
                    logoPreview.setImage(null);
                });
            }
        }).start();
    }

    // ====================== SET DATA (EDIT MODE) ======================
    public void setData(Cinema cinema) {
        this.cinema = cinema;
        
        if (cinema != null) {
            formTitleLabel.setText("CHỈNH SỬA RẠP");
            nameField.setText(cinema.getName());
            cityCombo.setValue(cinema.getCity());
            addressField.setText(cinema.getAddress());
            // phoneField.setText(cinema.getPhone());

            if (cinema.getLogoUrl() != null) {
                logoUrlField.setText(cinema.getLogoUrl());
                try {
                    Image image = new Image(cinema.getLogoUrl(), true);
                    logoPreview.setImage(image);
                } catch (Exception e) {
                    System.err.println("Failed to load logo: " + e.getMessage());
                }
            }
        } else {
            formTitleLabel.setText("THÊM RẠP MỚI");
        }
    }

    @FXML
    private void saveCinema() {
        if (!validateForm()) return;

        saveButton.setDisable(true);

        Cinema cinemaData = new Cinema();
        cinemaData.setName(nameField.getText().trim());
        cinemaData.setCity(cityCombo.getValue());
        cinemaData.setAddress(addressField.getText().trim());
        cinemaData.setLogoUrl(
            logoUrlField.getText() == null || logoUrlField.getText().isBlank()
                ? null
                : logoUrlField.getText().trim()
        );

        if (cinema == null) {
            createCinema(cinemaData);
        } else {
            updateCinema(cinema.getId(), cinemaData);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    // ====================== ACTIONS ======================
    // @FXML
    // private void selectLogo() {
    //     FileChooser fileChooser = new FileChooser();
    //     fileChooser.setTitle("Chọn Logo Rạp");
    //     fileChooser.getExtensionFilters().add(
    //         new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
    //     );
        
    //     selectedLogoFile = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        
    //     if (selectedLogoFile != null) {
    //         Image image = new Image(selectedLogoFile.toURI().toString());
    //         logoPreview.setImage(image);
    //     }
    // }

    // @FXML
    // private void saveCinema() {
    //     // Validate
    //     if (!validateForm()) {
    //         return;
    //     }

    //     // if (nameField.getText().trim().isEmpty()) {
    //     //     showValidationError("Vui lòng nhập tên rạp!");
    //     //     return;
    //     // }
        
    //     // if (cityCombo.getValue() == null) {
    //     //     showValidationError("Vui lòng chọn thành phố!");
    //     //     return;
    //     // }
        
    //     // if (addressField.getText().trim().isEmpty()) {
    //     //     showValidationError("Vui lòng nhập địa chỉ!");
    //     //     return;
    //     // }
        
    //     // TODO: Call API save
    //     // if (cinema == null) {
    //     //     CinemaService.create(newCinema);
    //     // } else {
    //     //     CinemaService.update(cinema.getId(), updatedCinema);
    //     // }
        
    //     // Callback to refresh parent list
    //     if (onSaveCallback != null) {
    //         onSaveCallback.run();
    //     }
        
    //     // Close form
    //     closeModal();
    // }
    private void createCinema(Cinema cinemaData) {
        System.out.println("🎬 Creating cinema: " + cinemaData.getName());
        
        cinemaApi.createCinema(cinemaData)
            .thenAccept(createdCinema -> {
                Platform.runLater(() -> {
                    System.out.println("✅ Cinema created: " + createdCinema.getId());
                    showSuccess("Đã thêm rạp thành công!");
                    
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    
                    closeModal();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    System.err.println("❌ Failed to create cinema: " + ex.getMessage());
                    showError("Không thể thêm rạp: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    private void updateCinema(String id, Cinema cinemaData) {
        System.out.println("🎬 Updating cinema: " + id);
        
        cinemaApi.updateCinema(id, cinemaData)
            .thenAccept(updatedCinema -> {
                Platform.runLater(() -> {
                    System.out.println("✅ Cinema updated: " + updatedCinema.getId());
                    showSuccess("Đã cập nhật rạp thành công!");
                    
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    
                    closeModal();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    System.err.println("❌ Failed to update cinema: " + ex.getMessage());
                    showError("Không thể cập nhật rạp: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    private boolean validateForm() {
        hideValidation();
        
        if (nameField.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập tên rạp!");
            return false;
        }
        
        if (cityCombo.getValue() == null || cityCombo.getValue().isEmpty()) {
            showValidationError("Vui lòng chọn thành phố!");
            return false;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập địa chỉ!");
            return false;
        }
        
        return true;
    }

    @FXML
    private void closeModal() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showValidationError(String message) {
        validationLabel.setText("⚠️ " + message);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
    }

    private void hideValidation() {
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Use show() instead of showAndWait()
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}