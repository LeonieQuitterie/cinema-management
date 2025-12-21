package com.cinema.controllers.admin.fnb;

import com.cinema.models.FoodCategory;
import com.cinema.models.FoodCombo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class FnbMenuController {

    @FXML
    private TableView<FoodCombo> comboTable;
    @FXML
    private TableColumn<FoodCombo, String> imageColumn;
    @FXML
    private TableColumn<FoodCombo, String> nameColumn;
    @FXML
    private TableColumn<FoodCombo, String> categoryColumn;
    @FXML
    private TableColumn<FoodCombo, String> priceColumn;
    @FXML
    private TableColumn<FoodCombo, String> descriptionColumn;
    @FXML
    private TableColumn<FoodCombo, String> statusColumn;
    @FXML
    private TableColumn<FoodCombo, Void> actionColumn;

    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private TextField searchField;

    @FXML
    private Label totalComboLabel;
    @FXML
    private Label availableComboLabel;
    @FXML
    private Label unavailableComboLabel;

    private ObservableList<FoodCombo> allCombos;
    private ObservableList<FoodCombo> filteredCombos;
    private NumberFormat currencyFormatter;

    @FXML
    public void initialize() {
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        allCombos = FXCollections.observableArrayList();
        filteredCombos = FXCollections.observableArrayList();

        setupTableColumns();
        setupFilters();
        loadSampleData(); // TODO: Replace with actual data loading
        updateStatistics();
    }

    private void setupTableColumns() {
        // Image Column - THAY THẾ TOÀN BỘ PHẦN NÀY
        imageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImageUrl()));

        imageColumn.setCellFactory(column -> new TableCell<FoodCombo, String>() {
            private final ImageView imageView = new ImageView();

            {
                // Cấu hình cố định cho ImageView - chỉ chạy 1 lần
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(false); // 🔥 QUAN TRỌNG: tắt giữ tỷ lệ → ảnh sẽ fill đầy khung
                imageView.setSmooth(true); // Chất lượng tốt khi scale
                imageView.setCache(true); // Cache để load nhanh hơn lần sau
                // Optional: bo góc nhẹ
                imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);");
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);

                if (empty || imageUrl == null || imageUrl.isBlank()) {
                    setGraphic(null);
                } else {
                    // Load ảnh bất đồng bộ (background loading = true)
                    // Kích thước yêu cầu là 80x80, preserveRatio = false, smooth = true
                    Image image = new Image(imageUrl, 80, 80, false, true, true);
                    imageView.setImage(image);
                    setGraphic(imageView);
                }
            }
        });
        // Name Column
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        // Category Column
        categoryColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getCategory().getDisplayName()));

        // Price Column
        priceColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(currencyFormatter.format(cellData.getValue().getPrice()) + " đ"));

        // Description Column
        descriptionColumn.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            if (desc != null && desc.length() > 50) {
                desc = desc.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(desc);
        });

        // Status Column
        statusColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().isAvailable() ? "Đang bán" : "Tạm ẩn"));
        statusColumn.setCellFactory(column -> new TableCell<FoodCombo, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(status);
                    if (status.equals("Đang bán")) {
                        statusLabel.getStyleClass().addAll("status-badge", "status-available");
                    } else {
                        statusLabel.getStyleClass().addAll("status-badge", "status-unavailable");
                    }
                    setGraphic(statusLabel);
                }
            }
        });

        // Action Column
        actionColumn.setCellFactory(column -> new TableCell<FoodCombo, Void>() {
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Xóa");
            private final HBox actionBox = new HBox(10);

            {
                editButton.getStyleClass().add("btn-edit");
                deleteButton.getStyleClass().add("btn-delete");
                actionBox.setAlignment(Pos.CENTER);
                actionBox.getChildren().addAll(editButton, deleteButton);

                editButton.setOnAction(event -> {
                    FoodCombo combo = getTableView().getItems().get(getIndex());
                    openEditForm(combo);
                });

                deleteButton.setOnAction(event -> {
                    FoodCombo combo = getTableView().getItems().get(getIndex());
                    handleDelete(combo);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });

        comboTable.setItems(filteredCombos);

        // Ẩn cột thừa
        comboTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupFilters() {
        // Category Filter
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("Tất cả danh mục");
        for (FoodCategory category : FoodCategory.values()) {
            categories.add(category.getDisplayName());
        }
        categoryFilter.setItems(categories);
        categoryFilter.getSelectionModel().selectFirst();

        // Status Filter
        ObservableList<String> statuses = FXCollections.observableArrayList(
                "Tất cả trạng thái", "Đang bán", "Tạm ẩn");
        statusFilter.setItems(statuses);
        statusFilter.getSelectionModel().selectFirst();

        // Add listeners
        categoryFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String selectedCategory = categoryFilter.getValue();
        String selectedStatus = statusFilter.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        List<FoodCombo> filtered = allCombos.stream()
                .filter(combo -> {
                    // Category filter
                    if (selectedCategory != null && !selectedCategory.equals("Tất cả danh mục")) {
                        if (!combo.getCategory().getDisplayName().equals(selectedCategory)) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(combo -> {
                    // Status filter
                    if (selectedStatus != null && !selectedStatus.equals("Tất cả trạng thái")) {
                        boolean isAvailable = selectedStatus.equals("Đang bán");
                        if (combo.isAvailable() != isAvailable) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(combo -> {
                    // Search filter
                    if (searchText.isEmpty()) {
                        return true;
                    }
                    return combo.getName().toLowerCase().contains(searchText);
                })
                .collect(Collectors.toList());

        filteredCombos.setAll(filtered);
        updateStatistics();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleRefresh() {
        categoryFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        searchField.clear();
        loadSampleData(); // TODO: Reload from database
        updateStatistics();
    }

    @FXML
    private void openAddForm() {
        openFormDialog(null);
    }

    private void openEditForm(FoodCombo combo) {
        openFormDialog(combo);
    }

    private void openFormDialog(FoodCombo combo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/views/admin/partials/fnb/fnb-form-content.fxml"));
            Parent root = loader.load();

            FnbFormController controller = loader.getController();
            controller.setParentController(this);

            if (combo != null) {
                controller.setEditMode(combo);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle(combo == null ? "Thêm Combo Mới" : "Sửa Combo");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(
                    "/styles/fnb-management.css").toExternalForm());

            stage.setScene(scene);
            controller.setStage(stage);
            stage.showAndWait();

        } catch (IOException e) {
            showError("Không thể mở form", "Lỗi khi tải form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDelete(FoodCombo combo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa combo này?");
        alert.setContentText(combo.getName() + "\n\nThao tác này không thể hoàn tác!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Delete from database
            allCombos.remove(combo);
            applyFilters();
            updateStatistics();
            showSuccess("Đã xóa combo thành công!");
        }
    }

    public void addCombo(FoodCombo combo) {
        // TODO: Save to database
        allCombos.add(combo);
        applyFilters();
        updateStatistics();
        showSuccess("Đã thêm combo mới thành công!");
    }

    public void updateCombo(FoodCombo oldCombo, FoodCombo newCombo) {
        // TODO: Update in database
        int index = allCombos.indexOf(oldCombo);
        if (index >= 0) {
            allCombos.set(index, newCombo);
            applyFilters();
            updateStatistics();
            showSuccess("Đã cập nhật combo thành công!");
        }
    }

    private void updateStatistics() {
        int total = allCombos.size();
        long available = allCombos.stream().filter(FoodCombo::isAvailable).count();
        long unavailable = total - available;

        totalComboLabel.setText(String.valueOf(total));
        availableComboLabel.setText(String.valueOf(available));
        unavailableComboLabel.setText(String.valueOf(unavailable));
    }

    private void loadSampleData() {
        // TODO: Replace with actual database loading
        allCombos.clear();

        // Sample data
        FoodCombo combo1 = new FoodCombo();
        combo1.setId("C001");
        combo1.setName("Combo 1 - Bắp + Nước");
        combo1.setDescription("1 bắp rang bơ size M + 1 nước ngọt size M");
        combo1.setPrice(120000);
        combo1.setImageUrl("https://i.pinimg.com/736x/76/ce/05/76ce05ec2225c5b0aad8234d231da8ed.jpg");
        combo1.setCategory(FoodCategory.COMBO);
        combo1.setAvailable(true);

        FoodCombo combo2 = new FoodCombo();
        combo2.setId("C002");
        combo2.setName("Combo 2 - Bắp lớn + 2 Nước");
        combo2.setDescription("1 bắp rang bơ size L + 2 nước ngọt size M");
        combo2.setPrice(180000);
        combo2.setImageUrl("https://i.pinimg.com/736x/76/ce/05/76ce05ec2225c5b0aad8234d231da8ed.jpg");
        combo2.setCategory(FoodCategory.COMBO);
        combo2.setAvailable(true);

        FoodCombo combo3 = new FoodCombo();
        combo3.setId("P001");
        combo3.setName("Bắp rang bơ");
        combo3.setDescription("Bắp rang bơ thơm ngon");
        combo3.setPrice(60000);
        combo3.setImageUrl("https://i.pinimg.com/736x/76/ce/05/76ce05ec2225c5b0aad8234d231da8ed.jpg");
        combo3.setCategory(FoodCategory.POPCORN);
        combo3.setAvailable(true);

        FoodCombo combo4 = new FoodCombo();
        combo4.setId("D001");
        combo4.setName("Pepsi");
        combo4.setDescription("Nước ngọt Pepsi size M");
        combo4.setPrice(35000);
        combo4.setImageUrl("https://i.pinimg.com/736x/76/ce/05/76ce05ec2225c5b0aad8234d231da8ed.jpg");
        combo4.setCategory(FoodCategory.DRINK);
        combo4.setAvailable(false);

        allCombos.addAll(combo1, combo2, combo3, combo4);
        applyFilters();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}