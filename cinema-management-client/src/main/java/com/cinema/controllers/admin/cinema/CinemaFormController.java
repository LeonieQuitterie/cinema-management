package com.cinema.controllers.admin.cinema;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.cinema.models.Cinema;

public class CinemaFormController implements Initializable {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> cityCombo;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private ImageView logoPreview;
    @FXML private Label validationLabel;

    private Cinema cinema; // Null = create, Not null = edit
    private File selectedLogoFile;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup city combo
        cityCombo.getItems().addAll("Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", "Hải Phòng");
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
        } else {
            formTitleLabel.setText("THÊM RẠP MỚI");
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    // ====================== ACTIONS ======================
    @FXML
    private void selectLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Logo Rạp");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        selectedLogoFile = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        
        if (selectedLogoFile != null) {
            Image image = new Image(selectedLogoFile.toURI().toString());
            logoPreview.setImage(image);
        }
    }

    @FXML
    private void saveCinema() {
        // Validate
        if (nameField.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập tên rạp!");
            return;
        }
        
        if (cityCombo.getValue() == null) {
            showValidationError("Vui lòng chọn thành phố!");
            return;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập địa chỉ!");
            return;
        }
        
        // TODO: Call API save
        // if (cinema == null) {
        //     CinemaService.create(newCinema);
        // } else {
        //     CinemaService.update(cinema.getId(), updatedCinema);
        // }
        
        // Callback to refresh parent list
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        // Close form
        closeModal();
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
}