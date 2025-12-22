package com.cinema.controllers.cinema;

import com.cinema.models.*;
import com.cinema.models.dto.*;
import com.cinema.utils.BookingApiClient;
import com.cinema.utils.BookingApiService;
import com.cinema.utils.CinemaBankApiClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    // ============ DATA RECEIVED FROM PREVIOUS SCREEN ============
    private Showtime currentShowtime;
    private String cinemaId;
    private List<Seat> selectedSeats = new ArrayList<>();
    private Map<String, ComboOrderItem> selectedCombos = new HashMap<>();
    private double ticketPrice = 0;

    // ============ SETTERS ============
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

    // ============ INITIALIZE ============
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // ✅ Đợi cho tất cả setter được gọi xong
        Platform.runLater(this::loadRealData);
    }

    // ============ LOAD REAL DATA FROM API ============
    private void loadRealData() {
        System.out.println("=== DEBUG: BookingConfirmationController.loadRealData() ===");

        // ✅ Debug dữ liệu nhận được
        System.out.println("currentShowtime: " + currentShowtime);
        if (currentShowtime != null) {
            System.out.println("  - Movie ID: " + currentShowtime.getMovieId());
            System.out.println("  - Screen ID: " + currentShowtime.getScreenId());
            System.out.println("  - Start Time: " + currentShowtime.getStartTime());
        } else {
            System.err.println("  ⚠️ currentShowtime is NULL!");
        }

        System.out.println("cinemaId: " + cinemaId);
        System.out.println("selectedSeats: " + selectedSeats.size());
        System.out.println("selectedCombos: " + selectedCombos.size());
        System.out.println("ticketPrice: " + ticketPrice);

        // Kiểm tra dữ liệu đã được set chưa
        if (currentShowtime == null) {
            showError("❌ Showtime is NULL!");
            return;
        }

        if (cinemaId == null) {
            showError("❌ Cinema ID is NULL!");
            return;
        }

        if (currentShowtime.getMovieId() == null) {
            showError("❌ Movie ID trong Showtime is NULL!");
            return;
        }

        // Hiển thị loading
        showLoading(true);

        // Chạy API calls trong background thread
        new Thread(() -> {
            try {
                buildBookingData();

                Platform.runLater(() -> {
                    displayBookingInfo();
                    showLoading(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Không thể tải thông tin booking: " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }

    // ============ BUILD BOOKING DATA FROM API ============
    private void buildBookingData() throws Exception {
        currentBooking = new Booking();

        System.out.println("🔄 Bắt đầu load booking data...");

        // ✅ BƯỚC 1: Lấy thông tin Movie
        System.out.println("📽️ Fetching movie: " + currentShowtime.getMovieId());
        fetchMovieDetails();

        // ✅ BƯỚC 2: Lấy thông tin Cinema
        System.out.println("🏢 Fetching cinema: " + cinemaId);
        fetchCinemaDetails();

        // ✅ BƯỚC 3: Lấy thông tin Screen
        System.out.println("🎥 Fetching screen: " + currentShowtime.getScreenId());
        fetchScreenDetails();

        // ✅ BƯỚC 4: Set thông tin Showtime
        System.out.println("⏰ Setting showtime details...");
        setShowtimeDetails();

        // ✅ BƯỚC 5: Set ghế đã chọn
        System.out.println("💺 Setting seats: " + selectedSeats.size() + " seats");
        setSeatDetails();

        // ✅ BƯỚC 6: Set combo đã chọn
        System.out.println("🍿 Setting combos: " + selectedCombos.size() + " combos");
        setComboDetails();

        // ✅ BƯỚC 7: Lấy thông tin Customer
        System.out.println("👤 Fetching customer...");
        fetchCustomerDetails();

        // ✅ BƯỚC 8: Tính tổng tiền
        System.out.println("💰 Calculating total price...");
        calculateTotalPrice();

        System.out.println("✅ Booking data loaded successfully!");
    }

    // ============ FETCH MOVIE DETAILS ============
    private void fetchMovieDetails() throws Exception {
        String movieId = currentShowtime.getMovieId();
        MovieDTO movie = BookingApiService.getMovie(movieId);

        currentBooking.setMovieTitle(movie.getTitle());
        currentBooking.setMoviePosterUrl(movie.getPosterUrl());
        currentBooking.setAgeRating(movie.getAgeRating());
        currentBooking.setAgeRatingDescription(movie.getAgeRatingDescription());

        System.out.println("  ✓ Movie: " + movie.getTitle());
    }

    // ============ FETCH CINEMA DETAILS ============
    private void fetchCinemaDetails() throws Exception {
        CinemaDTO cinema = BookingApiService.getCinema(cinemaId);

        currentBooking.setCinemaName(cinema.getName());
        currentBooking.setCinemaLogoUrl(cinema.getLogoUrl());

        System.out.println("  ✓ Cinema: " + cinema.getName());
    }

    // ============ FETCH SCREEN DETAILS ============
    private void fetchScreenDetails() throws Exception {
        String screenId = currentShowtime.getScreenId();
        ScreenDTO screen = BookingApiService.getScreen(screenId);

        currentBooking.setScreenName(screen.getName());

        System.out.println("  ✓ Screen: " + screen.getName());
    }

    // ============ SET SHOWTIME DETAILS ============
    private void setShowtimeDetails() {
        currentBooking.setShowtime(currentShowtime.getStartTime());
        currentBooking.setFormat(currentShowtime.getFormat());
    }

    // ============ SET SEAT DETAILS ============
    private void setSeatDetails() {
        // Convert List<Seat> → List<String> (seat numbers only)
        List<String> seatNumbers = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            seatNumbers.add(seat.getSeatNumber());
        }

        currentBooking.setSelectedSeats(seatNumbers);
        currentBooking.setSeatTotalPrice(ticketPrice);

        System.out.println("  ✓ Seats: " + String.join(", ", seatNumbers));
    }

    // ============ SET COMBO DETAILS ============
    private void setComboDetails() {
        // Convert Map<String, ComboOrderItem> → List<ComboOrderItem>
        List<ComboOrderItem> comboList = new ArrayList<>(selectedCombos.values());

        currentBooking.setCombos(comboList);

        // Tính tổng tiền combo
        double comboTotal = 0;
        for (ComboOrderItem item : comboList) {
            comboTotal += item.getFoodCombo().getPrice() * item.getQuantity();
        }

        currentBooking.setComboTotalPrice(comboTotal);

        System.out.println("  ✓ Combos: " + comboList.size() + " items, Total: " + comboTotal);
    }

    // ============ FETCH CUSTOMER DETAILS ============
    private void fetchCustomerDetails() throws Exception {
        CustomerDTO customerDTO = BookingApiService.getCurrentCustomer();

        // Convert DTO → Customer model
        Customer customer = new Customer(
                customerDTO.getId(),
                customerDTO.getFullName(),
                customerDTO.getPhoneNumber(),
                customerDTO.getEmail());

        currentBooking.setCustomer(customer);

        System.out.println("  ✓ Customer: " + customer.getFullName());
    }

    // ============ CALCULATE TOTAL PRICE ============
    private void calculateTotalPrice() {
        double total = currentBooking.getSeatTotalPrice()
                + currentBooking.getComboTotalPrice();

        currentBooking.setTotalPrice(total);

        System.out.println("  ✓ Total: " + total);
    }

    // ============ DISPLAY BOOKING INFO ON UI ============
    private void displayBookingInfo() {
        if (currentBooking == null) {
            System.err.println("❌ currentBooking is null!");
            return;
        }

        System.out.println("🖼️ Displaying booking info on UI...");

        // ==================== MOVIE POSTER ====================
        try {
            String posterUrl = currentBooking.getMoviePosterUrl();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                Image posterImg = new Image(posterUrl, true);
                moviePosterImage.setImage(posterImg);

                posterImg.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0 || posterImg.isError()) {
                        Platform.runLater(() -> {
                            applyFullFramePoster(moviePosterImage, posterImg);
                            applyRoundedImage(moviePosterImage, 8);
                        });
                    }
                });

                if (posterImg.getProgress() >= 1.0) {
                    applyFullFramePoster(moviePosterImage, posterImg);
                    applyRoundedImage(moviePosterImage, 8);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load poster image: " + e.getMessage());
        }

        // ==================== CINEMA LOGO ====================
        try {
            String logoUrl = currentBooking.getCinemaLogoUrl();
            if (logoUrl != null && !logoUrl.isEmpty()) {
                Image logoImg = new Image(logoUrl, true);
                cinemaLogoImage.setImage(logoImg);
                applyRoundedImage(cinemaLogoImage, 8);
            }
        } catch (Exception e) {
            System.err.println("Could not load cinema logo: " + e.getMessage());
        }

        // ==================== BASIC INFO ====================
        cinemaNameLabel.setText(currentBooking.getCinemaName());
        movieTitleLabel.setText(currentBooking.getMovieTitle());
        ageRatingBadge.setText(currentBooking.getAgeRating());
        ageRatingDescLabel.setText(currentBooking.getAgeRatingDescription());

        LocalDateTime showtime = currentBooking.getShowtime();
        LocalDateTime endTime = showtime.plusMinutes(120); // Adjust based on movie duration

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("'Thứ' E, dd/MM/yyyy", new Locale("vi", "VN"));

        showtimeLabel.setText(showtime.format(timeFmt) + " ~ " + endTime.format(timeFmt));
        dateLabel.setText(dateFmt.format(showtime));

        formatLabel.setText(currentBooking.getFormat());
        screenLabel.setText(currentBooking.getScreenName());
        seatsLabel.setText(String.join(", ", currentBooking.getSelectedSeats()));

        // ==================== COMBOS ====================
        displayCombos();

        // ==================== CUSTOMER ====================
        Customer customer = currentBooking.getCustomer();
        if (customer != null) {
            customerNameLabel.setText(customer.getFullName());
            customerPhoneLabel.setText(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "Chưa cập nhật");
            customerEmailLabel.setText(customer.getEmail() != null ? customer.getEmail() : "Chưa cập nhật");
        }

        // ==================== PRICES ====================
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

        System.out.println("✅ UI updated successfully!");
    }

    // ==================== DISPLAY COMBOS ====================
    private void displayCombos() {
        comboListContainer.getChildren().clear();

        if (currentBooking.getCombos() == null || currentBooking.getCombos().isEmpty()) {
            comboSection.setVisible(false);
            comboSection.setManaged(false);
            return;
        }

        comboSection.setVisible(true);
        comboSection.setManaged(true);

        for (ComboOrderItem item : currentBooking.getCombos()) {
            Label comboLabel = new Label(
                    String.format("%s x%d - %s đ",
                            item.getFoodCombo().getName(),
                            item.getQuantity(),
                            currencyFormat.format(item.getFoodCombo().getPrice() * item.getQuantity())));
            comboLabel.getStyleClass().add("combo-item-label");
            comboListContainer.getChildren().add(comboLabel);
        }
    }

    // ==================== HELPER METHODS ====================
    private void applyFullFramePoster(ImageView imageView, Image image) {
        if (image.isError())
            return;

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageRatio = imageWidth / imageHeight;

        double frameWidth = imageView.getFitWidth();
        double frameHeight = imageView.getFitHeight();
        double frameRatio = frameWidth / frameHeight;

        if (imageRatio > frameRatio) {
            // Ảnh rộng hơn → crop ngang
            imageView.setViewport(new javafx.geometry.Rectangle2D(
                    (imageWidth - imageHeight * frameRatio) / 2, 0,
                    imageHeight * frameRatio, imageHeight));
        } else {
            // Ảnh cao hơn → crop dọc
            imageView.setViewport(new javafx.geometry.Rectangle2D(
                    0, (imageHeight - imageWidth / frameRatio) / 2,
                    imageWidth, imageWidth / frameRatio));
        }
    }

    private void applyRoundedImage(ImageView imageView, double radius) {
        Rectangle clip = new Rectangle(
                imageView.getFitWidth(),
                imageView.getFitHeight());
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        imageView.setClip(clip);
    }

    private void showLoading(boolean show) {
        continueButton.setDisable(show);
        if (show) {
            continueButton.setText("Đang tải...");
        } else {
            continueButton.setText("Tiếp tục");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== BUTTON HANDLERS ====================
    @FXML
    private void handleBack() {
        // Navigate back to combo selection
        System.out.println("Back button clicked");
    }

    @FXML
    private void handleContinue() {
        try {
            System.out.println("=== Navigating to Payment ===");

            // ✅ Validate booking data
            if (currentBooking == null) {
                showError("Không có thông tin booking!");
                return;
            }

            if (cinemaId == null || cinemaId.isEmpty()) {
                showError("Không xác định được rạp chiếu!");
                return;
            }

            // 🔥 SET BOOKING ID Ở ĐÂY
            if (currentBooking.getId() == null || currentBooking.getId().isEmpty()) {
                currentBooking.setId("BOOK_" + System.currentTimeMillis());
            }

            currentBooking.setMovieId(currentShowtime.getMovieId());

            BookingDB bookingDB = new BookingDB(currentBooking);
            bookingDB.setCinemaId(cinemaId);
            bookingDB.setScreenId(currentShowtime.getScreenId());
            bookingDB.setShowtimeId(currentShowtime.getId());

            String customerId = currentBooking.getCustomer().getId();
            bookingDB.setCustomerId(customerId);

            System.out.println("📌 Booking ID: " + currentBooking.getId());
            System.out.println("📌 Cinema ID: " + cinemaId);
            System.out.println("📌 Total Price: " + currentBooking.getTotalPrice());

            // ✅ Disable button để tránh double-click
            continueButton.setDisable(true);
            continueButton.setText("Đang xử lý...");

            // ✅ Gọi API lấy bank info trong background thread
            new Thread(() -> {
                try {
                    // 1. Lấy thông tin ngân hàng
                    BankInfoDTO bankInfo = CinemaBankApiClient.getBankInfo(cinemaId);

                    if (bankInfo == null) {
                        Platform.runLater(() -> {
                            showError("Không thể lấy thông tin tài khoản ngân hàng!");
                            continueButton.setDisable(false);
                            continueButton.setText("Tiếp tục");
                        });
                        return;
                    }

                    // 2. Tạo QR Code URL động
                    String qrUrl = CinemaBankApiClient.generateDynamicQRUrl(
                            bankInfo,
                            currentBooking.getId(),
                            currentBooking.getTotalPrice());

                    // 3. Tạo PaymentInfo
                    PaymentInfo paymentInfo = new PaymentInfo(
                            bankInfo.getBankName(), // "Vietcombank"
                            bankInfo.getBankAccountHolder(), // "CONG TY TNHH CINEMA MANAGEMENT"
                            bankInfo.getBankAccountNumber(), // "0123456789"
                            currentBooking.getId(), // "BOOK20251209001" - Nội dung CK
                            qrUrl, // QR Code URL
                            currentBooking.getTotalPrice() // Số tiền
                    );

                    currentBooking.setPaymentInfo(paymentInfo);
                    currentBooking.setPaymentStatus(PaymentStatus.PENDING);
                    currentBooking.setBookingTime(LocalDateTime.now());
                    currentBooking.setPaymentDeadline(LocalDateTime.now().plusMinutes(15));

                    System.out.println("✅ Payment info created successfully");

                    // 3.5. LƯU BOOKING VÀO CSDL
                    try {
                        System.err.println(bookingDB.getMovieId());

                        System.err.println(bookingDB);
                        BookingApiClient.createBooking(bookingDB);
                        System.out.println("💾 Booking saved to database");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            showError("Không thể lưu booking: " + ex.getMessage());
                            continueButton.setDisable(false);
                            continueButton.setText("Tiếp tục");
                        });
                        return;
                    }

                    // 4. Chuyển sang Payment screen (trên JavaFX thread)
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/views/cinema/payment.fxml"));
                            Parent paymentRoot = loader.load();

                            // Get controller và truyền booking
                            PaymentController controller = loader.getController();
                            controller.setBooking(currentBooking);

                            System.out.println("✓ Data transferred to PaymentController");

                            // Chuyển scene
                            Stage stage = (Stage) continueButton.getScene().getWindow();
                            Scene currentScene = stage.getScene();
                            boolean wasFullScreen = stage.isFullScreen();

                            currentScene.setRoot(paymentRoot);

                            if (wasFullScreen) {
                                Platform.runLater(() -> {
                                    stage.setFullScreen(true);
                                    stage.setFullScreenExitHint("");
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            showError("Không thể chuyển sang trang thanh toán: " + e.getMessage());
                            continueButton.setDisable(false);
                            continueButton.setText("Tiếp tục");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showError("Lỗi xử lý: " + e.getMessage());
                        continueButton.setDisable(false);
                        continueButton.setText("Tiếp tục");
                    });
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi không xác định: " + e.getMessage());
            continueButton.setDisable(false);
            continueButton.setText("Tiếp tục");
        }
    }

    // Helper method để hiển thị error
    // private void showError(String message) {
    // Platform.runLater(() -> {
    // Alert alert = new Alert(Alert.AlertType.ERROR);
    // alert.setTitle("Lỗi");
    // alert.setHeaderText(null);
    // alert.setContentText(message);
    // alert.showAndWait();
    // });
    // }

    @FXML
    private void handleEditCustomer() {
        // Open customer edit dialog
        System.out.println("Edit customer button clicked");
    }
}