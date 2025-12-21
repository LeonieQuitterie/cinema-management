package com.cinema.controllers.admin.fnb;

import com.cinema.models.FoodCategory;
import com.cinema.models.FoodCombo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.UUID;

public class FnbFormController {

    @FXML private Label formTitleLabel;
    @FXML private ImageView imagePreview;
    @FXML private Label imageNameLabel;
    
    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    
    @FXML private ComboBox<FoodCategory> categoryCombo;
    @FXML private Label categoryErrorLabel;
    
    @FXML private TextField priceField;
    @FXML private Label priceErrorLabel;
    
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox availableCheckbox;
    @FXML private TextField idField;
    @FXML private Button saveButton;

    private Stage stage;
    private FnbMenuController parentController;
    private FoodCombo editingCombo;
    private String selectedImagePath;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        setupCategoryComboBox();
        setupValidation();
        setDefaultImage();
    }

    private void setupCategoryComboBox() {
        categoryCombo.getItems().addAll(FoodCategory.values());
        
        // Custom cell factory để hiển thị tên tiếng Việt
        categoryCombo.setCellFactory(lv -> new ListCell<FoodCategory>() {
            @Override
            protected void updateItem(FoodCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        
        // Custom button cell để hiển thị tên đã chọn
        categoryCombo.setButtonCell(new ListCell<FoodCategory>() {
            @Override
            protected void updateItem(FoodCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
    }

    private void setupValidation() {
        // Price field chỉ cho phép nhập số
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                priceField.setText(oldVal);
            }
        });

        // Description character limit
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 500) {
                descriptionArea.setText(oldVal);
            }
        });
    }

    private void setDefaultImage() {
        try {
            // Set placeholder image
            Image placeholderImage = new Image(getClass().getResourceAsStream(
                "/images/placeholder-food.png"));
            imagePreview.setImage(placeholderImage);
        } catch (Exception e) {
            // If placeholder doesn't exist, just leave it empty
            imageNameLabel.setText("Chưa chọn ảnh");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setParentController(FnbMenuController parentController) {
        this.parentController = parentController;
    }

    public void setEditMode(FoodCombo combo) {
        this.isEditMode = true;
        this.editingCombo = combo;
        
        formTitleLabel.setText("SỬA THÔNG TIN COMBO");
        saveButton.setText("Cập nhật");
        
        // Fill form with existing data
        idField.setText(combo.getId());
        nameField.setText(combo.getName());
        categoryCombo.setValue(combo.getCategory());
        priceField.setText(String.valueOf((int) combo.getPrice()));
        descriptionArea.setText(combo.getDescription());
        availableCheckbox.setSelected(combo.isAvailable());
        
        // Load image
        if (combo.getImageUrl() != null && !combo.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(combo.getImageUrl());
                imagePreview.setImage(image);
                imageNameLabel.setText(combo.getImageUrl());
                selectedImagePath = combo.getImageUrl();
            } catch (Exception e) {
                setDefaultImage();
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh combo");
        
        // Add extension filters
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            // Check file size (max 5MB)
            long fileSizeInMB = selectedFile.length() / (1024 * 1024);
            if (fileSizeInMB > 5) {
                showError("File quá lớn", "Vui lòng chọn ảnh có kích thước nhỏ hơn 5MB");
                return;
            }
            
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
                imageNameLabel.setText(selectedFile.getName());
                selectedImagePath = selectedFile.toURI().toString();
            } catch (Exception e) {
                showError("Lỗi tải ảnh", "Không thể tải ảnh: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveImage() {
        setDefaultImage();
        selectedImagePath = null;
        imageNameLabel.setText("Chưa chọn ảnh");
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        FoodCombo combo = new FoodCombo();
        
        if (isEditMode) {
            combo.setId(editingCombo.getId());
        } else {
            combo.setId(generateId());
        }
        
        combo.setName(nameField.getText().trim());
        combo.setCategory(categoryCombo.getValue());
        combo.setPrice(Double.parseDouble(priceField.getText().trim()));
        combo.setDescription(descriptionArea.getText().trim());
        combo.setAvailable(availableCheckbox.isSelected());
        combo.setImageUrl(selectedImagePath != null ? selectedImagePath : "");

        if (isEditMode) {
            parentController.updateCombo(editingCombo, combo);
        } else {
            parentController.addCombo(combo);
        }

        closeForm();
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Clear previous errors
        clearErrors();

        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            showFieldError(nameErrorLabel, "Vui lòng nhập tên combo");
            isValid = false;
        }

        // Validate category
        if (categoryCombo.getValue() == null) {
            showFieldError(categoryErrorLabel, "Vui lòng chọn danh mục");
            isValid = false;
        }

        // Validate price
        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            showFieldError(priceErrorLabel, "Vui lòng nhập giá");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    showFieldError(priceErrorLabel, "Giá phải lớn hơn 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError(priceErrorLabel, "Giá không hợp lệ");
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrors() {
        hideFieldError(nameErrorLabel);
        hideFieldError(categoryErrorLabel);
        hideFieldError(priceErrorLabel);
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideFieldError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void closeForm() {
        if (stage != null) {
            stage.close();
        }
    }

    private String generateId() {
        // Generate ID based on category
        FoodCategory category = categoryCombo.getValue();
        String prefix = "";
        
        switch (category) {
            case COMBO:
                prefix = "C";
                break;
            case POPCORN:
                prefix = "P";
                break;
            case DRINK:
                prefix = "D";
                break;
            case SNACK:
                prefix = "S";
                break;
        }
        
        // Generate random ID with prefix
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}