package com.cinema.controllers.cinema;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.text.NumberFormat;
import java.util.*;
import javafx.scene.shape.Rectangle;

public class ComboSelectionController {

    @FXML
    private Button backButton;
    @FXML
    private FlowPane comboFlowPane;
    @FXML
    private Label ticketPriceLabel;
    @FXML
    private Label comboPriceLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button continueButton;

    // Mock data từ trang trước (seat selection)
    private double ticketPrice = 225000; // Giá vé từ 3 ghế đã chọn (ví dụ)

    // Danh sách combo available
    private List<FoodCombo> availableCombos;

    // Danh sách combo đã chọn với số lượng
    private Map<String, ComboOrderItem> selectedCombos = new HashMap<>();

    private NumberFormat currencyFormat;

    @FXML
    public void initialize() {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Load mock combo data
        loadMockCombos();

        // Render combo cards
        renderComboCards();

        // Update price display
        updatePriceSummary();
    }

    private void loadMockCombos() {
        availableCombos = new ArrayList<>();

        // Combo 1: Combo Solo
        availableCombos.add(new FoodCombo(
                "CMB001",
                "Combo Solo",
                "1 Bắp rang bơ (M) + 1 Nước ngọt (M)",
                89000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg",
                FoodCategory.COMBO));

        // Combo 2: Combo Couple
        availableCombos.add(new FoodCombo(
                "CMB002",
                "Combo Couple",
                "1 Bắp rang bơ (L) + 2 Nước ngọt (M)",
                129000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg",
                FoodCategory.COMBO));

        // Combo 3: Combo Family
        availableCombos.add(new FoodCombo(
                "CMB003",
                "Combo Family",
                "2 Bắp rang bơ (L) + 4 Nước ngọt (M)",
                229000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg",
                FoodCategory.COMBO));

        // Combo 4: Combo Party
        availableCombos.add(new FoodCombo(
                "CMB004",
                "Combo Party",
                "3 Bắp rang bơ (L) + 6 Nước ngọt (M) + 2 Snack",
                349000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg🍿",
                FoodCategory.COMBO));

        // Combo 5: Bắp rang bơ
        availableCombos.add(new FoodCombo(
                "POP001",
                "Bắp rang bơ (L)",
                "Bắp rang thơm ngon, vị bơ đậm đà",
                65000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg",
                FoodCategory.POPCORN));

        // Combo 6: Nước ngọt
        availableCombos.add(new FoodCombo(
                "DRK001",
                "Coca Cola (L)",
                "Nước ngọt có ga Coca Cola size L",
                45000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg",
                FoodCategory.DRINK));

        // Combo 7: Snack
        availableCombos.add(new FoodCombo(
                "SNK001",
                "Snack Khoai tây",
                "Snack khoai tây chiên giòn tan",
                35000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg",
                FoodCategory.SNACK));

        // Combo 8: Combo VIP
        availableCombos.add(new FoodCombo(
                "CMB005",
                "Combo VIP Deluxe",
                "2 Bắp caramen (L) + 2 Trà sữa + 2 Hotdog",
                299000,
                "https://i.pinimg.com/1200x/77/ec/83/77ec8349ff9f792201787a2b9ac3f565.jpg",
                FoodCategory.COMBO));
    }

    private void renderComboCards() {
        comboFlowPane.getChildren().clear();

        for (FoodCombo combo : availableCombos) {
            VBox card = createComboCard(combo);
            comboFlowPane.getChildren().add(card);
        }
    }

    private VBox createComboCard(FoodCombo combo) {
        VBox card = new VBox(0);
        card.getStyleClass().add("combo-card");

        // Image Container (với placeholder emoji)
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("combo-image-container");

        // Load real image
        try {
            ImageView imageView = new ImageView();
            imageView.getStyleClass().add("combo-image");

            // Load image từ URL
            Image image = new Image(combo.getImageUrl(), true); // true = load ở background
            imageView.setImage(image);

            // Set size để fit với container
            imageView.setFitWidth(290);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(false); // Fill đầy container
            imageView.setSmooth(true);

            Rectangle clip = new Rectangle(290, 180);
            clip.setArcWidth(15); // Bo góc 12px
            clip.setArcHeight(15); // Bo góc 12px
            imageView.setClip(clip

            );

            imageContainer.getChildren().add(imageView);
        } catch (Exception e) {
            // Nếu load ảnh fail, dùng placeholder
            Label placeholderIcon = new Label("🍿");
            placeholderIcon.getStyleClass().add("combo-placeholder");
            imageContainer.getChildren().add(placeholderIcon);
        }
        // Info Container
        VBox infoBox = new VBox(10);
        infoBox.getStyleClass().add("combo-info");

        // Combo Name
        Label nameLabel = new Label(combo.getName());
        nameLabel.getStyleClass().add("combo-name");
        nameLabel.setStyle("-fx-font-size: 16px;");
        nameLabel.setWrapText(true);

        // Combo Description
        Label descLabel = new Label(combo.getDescription());
        descLabel.getStyleClass().add("combo-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(260);

        // Price
        Label priceLabel = new Label(currencyFormat.format(combo.getPrice()) + " đ");
        priceLabel.getStyleClass().add("combo-price");
        priceLabel.setStyle("-fx-font-size: 18px;");

        // Quantity Control
        HBox quantityControl = createQuantityControl(combo);

        // Add all to info box
        infoBox.getChildren().addAll(nameLabel, descLabel, priceLabel, quantityControl);

        // Add to card
        card.getChildren().addAll(imageContainer, infoBox);

        return card;
    }

    private HBox createQuantityControl(FoodCombo combo) {
        HBox controlBox = new HBox(15);
        controlBox.getStyleClass().add("quantity-control");

        controlBox.setAlignment(Pos.CENTER);

        // Decrease button
        Button decreaseBtn = new Button("-");
        decreaseBtn.getStyleClass().add("quantity-button");
        decreaseBtn.setDisable(true);

        // Quantity display
        Label quantityLabel = new Label("0");
        quantityLabel.getStyleClass().add("quantity-display");

        // Increase button
        Button increaseBtn = new Button("+");

        increaseBtn.getStyleClass().add("quantity-button");

        // Event handlers
        increaseBtn.setOnAction(e -> {
            ComboOrderItem orderItem = selectedCombos.get(combo.getId());
            if (orderItem == null) {
                orderItem = new ComboOrderItem(combo, 0);
                selectedCombos.put(combo.getId(), orderItem);
            }

            orderItem.incrementQuantity();
            quantityLabel.setText(String.valueOf(orderItem.getQuantity()));
            decreaseBtn.setDisable(false);

            updatePriceSummary();
        });

        decreaseBtn.setOnAction(e -> {
            ComboOrderItem orderItem = selectedCombos.get(combo.getId());
            if (orderItem != null) {
                orderItem.decrementQuantity();
                quantityLabel.setText(String.valueOf(orderItem.getQuantity()));

                if (orderItem.getQuantity() == 0) {
                    decreaseBtn.setDisable(true);
                    selectedCombos.remove(combo.getId());
                }

                updatePriceSummary();
            }
        });

        controlBox.getChildren().addAll(decreaseBtn, quantityLabel, increaseBtn);

        return controlBox;
    }

    private void updatePriceSummary() {
        // Update ticket price
        ticketPriceLabel.setText(currencyFormat.format(ticketPrice) + " đ");

        // Calculate combo total
        double comboTotal = 0;
        for (ComboOrderItem item : selectedCombos.values()) {
            comboTotal += item.getTotalPrice();
        }
        comboPriceLabel.setText(currencyFormat.format(comboTotal) + " đ");

        // Calculate grand total
        double grandTotal = ticketPrice + comboTotal;
        totalPriceLabel.setText(currencyFormat.format(grandTotal) + " đ");
    }

    @FXML
    private void handleBackButton() {
        System.out.println("Back to seat selection");
        // TODO: Navigate back to seat selection page
    }
    @FXML
    private void handleContinue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/booking-confirmation.fxml"));
            Parent newRoot = loader.load();

            Stage stage = (Stage) continueButton.getScene().getWindow();
            Scene currentScene = stage.getScene();
            boolean wasFullScreen = stage.isFullScreen();

            currentScene.setRoot(newRoot);  // chuyển trang mượt

            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");  // ẩn dòng "Press ESC..."
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Inner classes for mock data

    public static class FoodCombo {
        private String id;
        private String name;
        private String description;
        private double price;
        private String imageUrl;
        private FoodCategory category;

        public FoodCombo(String id, String name, String description, double price,
                String imageUrl, FoodCategory category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.imageUrl = imageUrl;
            this.category = category;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public double getPrice() {
            return price;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public FoodCategory getCategory() {
            return category;
        }
    }

    public enum FoodCategory {
        COMBO("Combo"),
        POPCORN("Bắp rang"),
        DRINK("Nước uống"),
        SNACK("Đồ ăn vặt");

        private final String displayName;

        FoodCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class ComboOrderItem {
        private FoodCombo foodCombo;
        private int quantity;

        public ComboOrderItem(FoodCombo foodCombo, int quantity) {
            this.foodCombo = foodCombo;
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return foodCombo.getPrice() * quantity;
        }

        public FoodCombo getFoodCombo() {
            return foodCombo;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void incrementQuantity() {
            this.quantity++;
        }

        public void decrementQuantity() {
            if (this.quantity > 0) {
                this.quantity--;
            }
        }
    }
}