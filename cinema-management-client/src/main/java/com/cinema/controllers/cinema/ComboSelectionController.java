package com.cinema.controllers.cinema;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

import com.cinema.models.ComboOrderItem;
import com.cinema.models.FoodCombo;
import com.cinema.models.Seat;
import com.cinema.models.Showtime;
import com.cinema.utils.FoodComboApiClient;

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

    // Danh sách combo available
    private List<FoodCombo> availableCombos = new ArrayList<>();

    // Danh sách combo đã chọn với số lượng
    private Map<String, ComboOrderItem> selectedCombos = new HashMap<>();

    private NumberFormat currencyFormat;

    // === DỮ LIỆU NHẬN TỪ TRANG CHỌN GHẾ (SeatSelection) ===
    private String cinemaId;
    private double ticketPrice = 0;
    private Showtime currentShowtime;
    private List<Seat> selectedSeats = new ArrayList<>();

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public void setShowtime(Showtime showtime) {
        this.currentShowtime = showtime;
    }

    public void setSelectedSeats(List<Seat> seats) {
        this.selectedSeats = seats;
    }

    @FXML
    public void initialize() {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        // loadRealCombos();

        // Update price display
        updatePriceSummary();
    }

    public void initData() {
        loadRealCombos();
    }

    private void loadRealCombos() {
        if (cinemaId == null || cinemaId.trim().isEmpty()) {
            showErrorMessage("Không xác định được rạp chiếu phim!");
            return;
        }

        comboFlowPane.getChildren().clear();
        Label loadingLabel = new Label("Đang tải danh sách combo...");
        loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888888; -fx-padding: 20px;");
        loadingLabel.setAlignment(Pos.CENTER);
        comboFlowPane.getChildren().add(loadingLabel);

        new Thread(() -> {
            List<FoodCombo> combos = FoodComboApiClient.getFoodCombosByCinemaId(cinemaId);

            Platform.runLater(() -> {
                comboFlowPane.getChildren().clear();

                if (combos == null || combos.isEmpty()) {
                    Label emptyLabel = new Label("Hiện chưa có combo nào.\nBạn vẫn có thể tiếp tục đặt vé.");
                    emptyLabel.setStyle(
                            "-fx-font-size: 16px; -fx-text-fill: #FF9800; -fx-text-alignment: center; -fx-padding: 20px;");
                    emptyLabel.setAlignment(Pos.CENTER);
                    comboFlowPane.getChildren().add(emptyLabel);
                } else {
                    availableCombos = combos.stream()
                            .filter(FoodCombo::isAvailable)
                            .toList();

                    if (availableCombos.isEmpty()) {
                        Label noAvailable = new Label("Hiện không có combo nào đang bán.");
                        noAvailable.setStyle("-fx-font-size: 16px; -fx-text-fill: #FF9800; -fx-padding: 20px;");
                        noAvailable.setAlignment(Pos.CENTER);
                        comboFlowPane.getChildren().add(noAvailable);
                    } else {
                        renderComboCards();
                    }
                }

                updatePriceSummary();
            });
        }).start();
    }

    private void showErrorMessage(String message) {
        comboFlowPane.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-text-alignment: center;");
        errorLabel.setAlignment(Pos.CENTER);
        comboFlowPane.getChildren().add(errorLabel);
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
            imageView.setClip(clip);

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
            System.out.println("=== DEBUG: ComboSelectionController.handleContinue() ===");

            // ✅ Debug currentShowtime
            System.out.println("currentShowtime: " + currentShowtime);
            if (currentShowtime != null) {
                System.out.println("  - Movie ID: " + currentShowtime.getMovieId());
                System.out.println("  - Screen ID: " + currentShowtime.getScreenId());
                System.out.println("  - Start Time: " + currentShowtime.getStartTime());
                System.out.println("  - Format: " + currentShowtime.getFormat());
                System.out.println("  - Base Price: " + currentShowtime.getBasePrice());
            } else {
                System.err.println("  ⚠️ currentShowtime is NULL!");
            }

            System.out.println("cinemaId: " + cinemaId);
            System.out.println("selectedSeats: " + selectedSeats.size());
            System.out.println("selectedCombos: " + selectedCombos.size());
            System.out.println("ticketPrice: " + ticketPrice);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/booking-confirmation.fxml"));
            Parent newRoot = loader.load();

            BookingConfirmationController controller = loader.getController();

            // Set dữ liệu
            controller.setShowtime(currentShowtime);
            controller.setCinemaId(cinemaId);
            controller.setSelectedSeats(new ArrayList<>(selectedSeats));
            controller.setSelectedCombos(new HashMap<>(selectedCombos));
            controller.setTicketPrice(ticketPrice);

            System.out.println("✓ Data transferred to BookingConfirmationController");

            Stage stage = (Stage) continueButton.getScene().getWindow();
            Scene currentScene = stage.getScene();
            boolean wasFullScreen = stage.isFullScreen();

            currentScene.setRoot(newRoot);

            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
         
        }
    }

}