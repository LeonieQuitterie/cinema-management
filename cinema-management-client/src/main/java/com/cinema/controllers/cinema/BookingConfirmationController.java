package com.cinema.controllers.cinema;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.cinema.models.*;

public class BookingConfirmationController implements Initializable {

    @FXML
    private Button backButton;
    @FXML
    private ImageView moviePosterImage;
    @FXML
    private ImageView cinemaLogoImage;
    @FXML
    private Label cinemaNameLabel;
    @FXML
    private Label movieTitleLabel;
    @FXML
    private Label ageRatingBadge;
    @FXML
    private Label ageRatingDescLabel;
    @FXML
    private Label showtimeLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label formatLabel;
    @FXML
    private Label screenLabel;
    @FXML
    private Label seatsLabel;

    @FXML
    private VBox comboListContainer;
    @FXML
    private VBox comboSection;

    @FXML
    private Button editCustomerButton;
    @FXML
    private Label customerNameLabel;
    @FXML
    private Label customerPhoneLabel;
    @FXML
    private Label customerEmailLabel;

    @FXML
    private HBox comboPriceRow;
    @FXML
    private Label seatPriceLabel;
    @FXML
    private Label comboPriceLabel;
    @FXML
    private Label totalPriceLabel;

    @FXML
    private Button continueButton;

    private Booking currentBooking;
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");

    private Showtime currentShowtime;
    private String cinemaId;
    private List<Seat> selectedSeats = new ArrayList<>();
    private Map<String, ComboOrderItem> selectedCombos = new HashMap<>();
    private double ticketPrice = 0;

    // Setters
    public void setShowtime(Showtime showtime) {
        this.currentShowtime = showtime;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public void setSelectedSeats(List<Seat> seats) {
        this.selectedSeats = seats;
    }

    public void setSelectedCombos(Map<String, ComboOrderItem> combos) {
        this.selectedCombos = combos;
    }

    public void setTicketPrice(double price) {
        this.ticketPrice = price;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMockData();
        displayBookingInfo();
    }

    private void loadMockData() {
        // Mock Customer
        Customer customer = new Customer(
                "CUST001",
                "Nguyễn Văn An",
                "0901234567",
                "nguyenvanan@gmail.com");

        // Mock Food Combos
        FoodCombo combo1 = new FoodCombo();
        combo1.setId("COMBO001");
        combo1.setName("Combo Couple");
        combo1.setDescription("2 Bắp lớn + 2 Nước lớn");
        combo1.setPrice(150000);
        combo1.setImageUrl("https://i.pinimg.com/1200x/53/a9/20/53a920cce6c733e590fac6356a74d954.jpg");
        combo1.setCategory(FoodCategory.COMBO);

        FoodCombo combo2 = new FoodCombo();
        combo2.setId("COMBO002");
        combo2.setName("Combo Solo");
        combo2.setDescription("1 Bắp vừa + 1 Nước vừa");
        combo2.setPrice(85000);
        combo2.setImageUrl("https://i.pinimg.com/1200x/53/a9/20/53a920cce6c733e590fac6356a74d954.jpg");
        combo2.setCategory(FoodCategory.COMBO);

        List<ComboOrderItem> combos = Arrays.asList(
                new ComboOrderItem(combo1, 1),
                new ComboOrderItem(combo2, 2));

        // Mock Booking
        currentBooking = new Booking();
        currentBooking.setId("BOOK20251209001");
        currentBooking.setMovieTitle("Kraven The Hunter");
        currentBooking.setMoviePosterUrl("https://i.pinimg.com/1200x/26/8c/5d/268c5d2c935044e7b61644582ad4f426.jpg");
        currentBooking.setAgeRating("16+");
        currentBooking.setAgeRatingDescription("Phim được phổ biến đến người xem từ đủ 16 tuổi trở lên");

        currentBooking.setCinemaName("CGV Vincom Đà Nẵng");
        currentBooking.setCinemaLogoUrl("https://i.pinimg.com/1200x/53/a9/20/53a920cce6c733e590fac6356a74d954.jpg");

        currentBooking.setScreenName("Cinema 4");
        currentBooking.setShowtime(LocalDateTime.of(2025, 12, 9, 20, 20));
        currentBooking.setFormat("2D phụ đề");

        currentBooking.setSelectedSeats(Arrays.asList("H13"));
        currentBooking.setSeatTotalPrice(85000);

        currentBooking.setCombos(combos);
        currentBooking.setComboTotalPrice(320000); // 150k + 85k*2

        currentBooking.setCustomer(customer);
        currentBooking.setTotalPrice(405000); // 85k + 320k
    }

    private void displayBookingInfo() {
        if (currentBooking == null)
            return;

        // ==================== MOVIE POSTER - FULL KHUNG + CROP ĐẸP
        // ====================
        try {
            Image posterImg = new Image(currentBooking.getMoviePosterUrl(), true);
            moviePosterImage.setImage(posterImg);

            // Đảm bảo crop + bo góc khi ảnh load xong (cả lần đầu và khi cache)
            posterImg.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 || posterImg.isError()) {
                    Platform.runLater(() -> {
                        applyFullFramePoster(moviePosterImage, posterImg);
                        applyRoundedImage(moviePosterImage, 8);
                    });
                }
            });

            // Trường hợp ảnh đã được cache → load ngay lập tức
            if (posterImg.getProgress() >= 1.0) {
                applyFullFramePoster(moviePosterImage, posterImg);
                applyRoundedImage(moviePosterImage, 8);
            }

        } catch (Exception e) {
            System.out.println("Could not load poster image: " + e.getMessage());
        }

        // ==================== CINEMA LOGO ====================
        try {
            Image logoImg = new Image(currentBooking.getCinemaLogoUrl(), true);
            cinemaLogoImage.setImage(logoImg);
            applyRoundedImage(cinemaLogoImage, 8);
        } catch (Exception e) {
            System.out.println("Could not load cinema logo");
        }

        // ==================== THÔNG TIN CƠ BẢN (giữ nguyên) ====================
        cinemaNameLabel.setText(currentBooking.getCinemaName());
        movieTitleLabel.setText(currentBooking.getMovieTitle());
        ageRatingBadge.setText(currentBooking.getAgeRating());
        ageRatingDescLabel.setText(currentBooking.getAgeRatingDescription());

        LocalDateTime showtime = currentBooking.getShowtime();
        LocalDateTime endTime = showtime.plusMinutes(116);

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("'Thứ' E, dd/MM/yyyy", new Locale("vi", "VN"));

        showtimeLabel.setText(showtime.format(timeFmt) + " ~ " + endTime.format(timeFmt));
        dateLabel.setText(dateFmt.format(showtime));

        formatLabel.setText(currentBooking.getFormat());
        screenLabel.setText(currentBooking.getScreenName());
        seatsLabel.setText(String.join(", ", currentBooking.getSelectedSeats()));

        displayCombos();

        Customer customer = currentBooking.getCustomer();
        if (customer != null) {
            customerNameLabel.setText(customer.getFullName());
            customerPhoneLabel.setText(customer.getPhoneNumber());
            customerEmailLabel.setText(customer.getEmail());
        }

        seatPriceLabel.setText(currencyFormat.format(currentBooking.getSeatTotalPrice()) + " đ");

        if (currentBooking.getCombos() != null && !currentBooking.getCombos().isEmpty()) {
            comboPriceLabel.setText(currencyFormat.format(currentBooking.getComboTotalPrice()) + " đ");
            comboPriceRow.setVisible(true);
            comboPriceRow.setManaged(true);
        } else {
            comboPriceRow.setVisible(false);
            comboPriceRow.setManaged(false);
        }

        totalPriceLabel.setText(currencyFormat.format(currentBooking.getTotalPrice()) + " đ");
    }

    // ==================== METHOD MỚI - CHỈ 1 LẦN DÙNG LẠI NHIỀU NƠI
    // ====================
    private void applyFullFramePoster(ImageView imageView, Image image) {
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0)
            return;

        double targetW = 270.0;
        double targetH = 400.0;
        double imgW = image.getWidth();
        double imgH = image.getHeight();
        double containerRatio = targetW / targetH;
        double imgRatio = imgW / imgH;

        Rectangle2D viewport;
        if (imgRatio > containerRatio) {
            // Ảnh rộng hơn → crop 2 bên
            double scaledW = imgH * containerRatio;
            viewport = new Rectangle2D((imgW - scaledW) / 2, 0, scaledW, imgH);
        } else {
            // Ảnh cao hơn hoặc vuông → crop trên/dưới
            double scaledH = imgW / containerRatio;
            viewport = new Rectangle2D(0, (imgH - scaledH) / 2, imgW, scaledH);
        }

        imageView.setViewport(viewport);
    }

    private void applyRoundedImage(ImageView imageView, double radius) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageView.fitWidthProperty());
        clip.heightProperty().bind(imageView.fitHeightProperty());
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        imageView.setClip(clip);
    }

    private void displayCombos() {
        comboListContainer.getChildren().clear();

        if (currentBooking.getCombos() == null || currentBooking.getCombos().isEmpty()) {
            comboSection.setVisible(false);
            comboSection.setManaged(false);
            return;
        }

        for (ComboOrderItem item : currentBooking.getCombos()) {
            HBox comboBox = createComboItem(item);
            comboListContainer.getChildren().add(comboBox);
        }
    }

    private HBox createComboItem(ComboOrderItem item) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("combo-item-box");

        // Combo Image
        ImageView comboImage = new ImageView();
        comboImage.setFitWidth(60);
        comboImage.setFitHeight(60);
        comboImage.setPreserveRatio(true);
        comboImage.getStyleClass().add("combo-image");

        try {
            Image img = new Image(item.getFoodCombo().getImageUrl(), true);
            comboImage.setImage(img);
        } catch (Exception e) {
            System.out.println("Could not load combo image");
        }

        // Combo Info
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label(item.getFoodCombo().getName());
        nameLabel.getStyleClass().add("combo-name");

        Label quantityLabel = new Label("Số lượng: " + item.getQuantity());
        quantityLabel.getStyleClass().add("combo-quantity");

        infoBox.getChildren().addAll(nameLabel, quantityLabel);

        // Price
        Label priceLabel = new Label(currencyFormat.format(item.getTotalPrice()) + " đ");
        priceLabel.getStyleClass().add("combo-price");

        container.getChildren().addAll(comboImage, infoBox, priceLabel);

        return container;
    }

    @FXML
    private void handleBack() {
        System.out.println("Back button clicked");
        // TODO: Navigate back to previous screen
    }

    @FXML
    private void handleEditCustomer() {
        System.out.println("Edit customer info clicked");
        // TODO: Open dialog to edit customer information

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chỉnh sửa thông tin");
        alert.setHeaderText("Chức năng đang phát triển");
        alert.setContentText("Tính năng chỉnh sửa thông tin người nhận sẽ được bổ sung sau.");
        alert.showAndWait();
    }

    @FXML
    private void handleContinue() {
        try {
            // Load trang booking-success.fxml
            Parent newRoot = FXMLLoader.load(
                    getClass().getResource("/views/cinema/payment.fxml"));

            // Lấy Stage hiện tại
            Stage stage = (Stage) continueButton.getScene().getWindow();
            Scene scene = stage.getScene();
            boolean isFullScreen = stage.isFullScreen();

            // Chuyển trang mượt mà
            scene.setRoot(newRoot);

            // Giữ lại chế độ fullscreen nếu đang bật
            if (isFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint(""); // ẩn dòng "Press ESC to exit full screen"
                });
            }

            System.out.println("Đã chuyển sang trang Booking Success");

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể chuyển trang");
            alert.setContentText("Vui lòng kiểm tra file booking-success.fxml có tồn tại không.");
            alert.showAndWait();
        }
    }
}