package com.cinema.controllers.cinema;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.cinema.models.*;

public class PaymentController implements Initializable {

    @FXML
    private Button backButton;
    @FXML
    private Label bookingCodeLabel;
    @FXML
    private ImageView moviePosterImage;
    @FXML
    private Label movieTitleLabel;
    @FXML
    private Label cinemaInfoLabel;
    @FXML
    private Label showtimeLabel;
    @FXML
    private Label seatsLabel;
    @FXML
    private Label combosLabel;
    @FXML
    private Label totalPriceLabel;

    @FXML
    private ImageView qrCodeImage;
    @FXML
    private Label bankNameLabel;
    @FXML
    private Label accountHolderLabel;
    @FXML
    private Label accountNumberLabel;
    @FXML
    private Label transferContentLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private Button copyButton;

    @FXML
    private Label countdownLabel;
    @FXML
    private Button confirmPaymentButton;

    private Booking currentBooking;
    private Timeline countdownTimeline;
    private int remainingSeconds = 900; // 15 phút = 900 giây

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Tạo mock data
        currentBooking = createMockBooking();

        // Load dữ liệu lên UI
        loadBookingData();

        // Bắt đầu đếm ngược
        startCountdown();
    }

    private Booking createMockBooking() {
        Booking booking = new Booking();

        // Thông tin cơ bản
        booking.setId("BOOK20251209001");
        booking.setMovieId("M001");
        booking.setMovieTitle("Võ Sĩ Giác Đấu II");
        booking.setMoviePosterUrl("https://i.pinimg.com/736x/a4/ba/63/a4ba6312644cea9548c2df117832d1ea.jpg");
        booking.setAgeRating("C18");
        booking.setAgeRatingDescription("Phim dành cho khán giả từ đủ 18 tuổi trở lên");

        // Thông tin rạp
        booking.setCinemaId("C001");
        booking.setCinemaName("CGV Vincom");
        booking.setCinemaLogoUrl("https://i.pinimg.com/736x/a4/ba/63/a4ba6312644cea9548c2df117832d1ea.jpg");
        booking.setScreenName("Phòng 5");

        // Thời gian
        LocalDateTime showtime = LocalDateTime.of(2025, 12, 9, 20, 20);
        booking.setShowtime(showtime);
        booking.setFormat("2D phụ đề");
        booking.setBookingTime(LocalDateTime.now());
        booking.setPaymentDeadline(LocalDateTime.now().plusMinutes(15));

        // Ghế
        booking.setSelectedSeats(Arrays.asList("H13"));
        booking.setSeatTotalPrice(100000);

        // Combo
        List<ComboOrderItem> combos = new ArrayList<>();

        FoodCombo comboCouple = new FoodCombo();
        comboCouple.setId("FC001");
        comboCouple.setName("Combo Couple");
        comboCouple.setDescription("2 Bắp lớn + 2 Nước lớn");
        comboCouple.setPrice(150000);
        comboCouple.setCategory(FoodCategory.COMBO);

        FoodCombo comboSolo = new FoodCombo();
        comboSolo.setId("FC002");
        comboSolo.setName("Combo Solo");
        comboSolo.setDescription("1 Bắp + 1 Nước");
        comboSolo.setPrice(75000);
        comboSolo.setCategory(FoodCategory.COMBO);

        combos.add(new ComboOrderItem(comboCouple, 1));
        combos.add(new ComboOrderItem(comboSolo, 2));

        booking.setCombos(combos);
        booking.setComboTotalPrice(300000);

        // Khách hàng
        Customer customer = new Customer();
        customer.setId("CUST001");
        customer.setFullName("Nguyễn Văn A");
        customer.setPhoneNumber("0912345678");
        customer.setEmail("nguyenvana@example.com");
        booking.setCustomer(customer);

        // Tổng tiền
        booking.setTotalPrice(405000);

        // Thông tin thanh toán
        PaymentInfo paymentInfo = new PaymentInfo(
                "Vietcombank",
                "CÔNG TY TNHH CINEMA PRO",
                "9999888877777",
                booking.getId(),
                "https://via.placeholder.com/400x400/ffffff/000000?text=QR+CODE",
                booking.getTotalPrice());
        booking.setPaymentInfo(paymentInfo);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        return booking;
    }

    private void loadBookingData() {
        // Mã đặt vé
        bookingCodeLabel.setText(currentBooking.getId());

        // Thông tin phim
        try {
            Image posterImage = new Image(currentBooking.getMoviePosterUrl(), true);
            moviePosterImage.setImage(posterImage);
        } catch (Exception e) {
            System.err.println("Không thể load poster: " + e.getMessage());
        }

        movieTitleLabel.setText(currentBooking.getMovieTitle());
        cinemaInfoLabel.setText(currentBooking.getCinemaName() + " • " + currentBooking.getScreenName());

        // Format showtime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy – HH:mm", new Locale("vi", "VN"));
        showtimeLabel.setText(currentBooking.getShowtime().format(formatter));

        seatsLabel.setText(currentBooking.getSelectedSeatsString());
        combosLabel.setText(currentBooking.getCombosDescription());
        totalPriceLabel.setText(currentBooking.getFormattedTotalPrice());

        // Thông tin thanh toán
        PaymentInfo paymentInfo = currentBooking.getPaymentInfo();
        if (paymentInfo != null) {
            try {
                Image qrImage = new Image(paymentInfo.getQrCodeUrl(), true);
                qrCodeImage.setImage(qrImage);
            } catch (Exception e) {
                System.err.println("Không thể load QR code: " + e.getMessage());
            }

            bankNameLabel.setText(paymentInfo.getBankName());
            accountHolderLabel.setText(paymentInfo.getAccountHolder());
            accountNumberLabel.setText(paymentInfo.getFormattedAccountNumber());
            transferContentLabel.setText(paymentInfo.getTransferContent());
            amountLabel.setText(paymentInfo.getFormattedAmount());
        }
    }

    private void startCountdown() {
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;

            if (remainingSeconds <= 0) {
                countdownTimeline.stop();
                handlePaymentExpired();
            } else {
                updateCountdownLabel();
            }
        }));

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();

        updateCountdownLabel();
    }

    private void updateCountdownLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        countdownLabel.setText(String.format("%02d:%02d", minutes, seconds));

        // Đổi màu khi sắp hết giờ (dưới 3 phút)
        if (remainingSeconds < 180) {
            countdownLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 26px;");
        }
    }

    @FXML
    private void handleBack() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Quay lại trang trước?");
        alert.setContentText("Thời gian giữ ghế sẽ tiếp tục đếm ngược. Bạn có chắc muốn quay lại?");

        alert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK")) {
                System.out.println("Quay lại trang xác nhận...");
                // TODO: Chuyển về trang xác nhận
            } else {
                // Tiếp tục đếm ngược
                if (countdownTimeline != null) {
                    countdownTimeline.play();
                }
            }
        });
    }

    @FXML
    private void handleCopyTransferContent() {
        String content = currentBooking.getPaymentInfo().getTransferContent();

        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);

        // Hiển thị thông báo
        copyButton.setText("✓ Đã copy!");

        // Reset text sau 2 giây
        Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            copyButton.setText("📋 Copy");
        }));
        resetTimeline.play();
    }

    @FXML
    private void handleConfirmPayment() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        confirmPaymentButton.setDisable(true);
        confirmPaymentButton.setText("Đang xác nhận...");

        // Giả lập thanh toán 3 giây
        Timeline checkPaymentTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            currentBooking.setPaymentStatus(PaymentStatus.PAID);

            // Dùng Platform.runLater để tránh lỗi showAndWait trong Timeline
            Platform.runLater(() -> {
             
                // 2. Sau khi bấm OK alert → mới chuyển trang
                try {
                    Parent successRoot = FXMLLoader.load(
                            getClass().getResource("/views/cinema/booking-success.fxml"));

                    Stage stage = (Stage) confirmPaymentButton.getScene().getWindow();
                    Scene scene = stage.getScene();
                    boolean isFullScreen = stage.isFullScreen();

                    scene.setRoot(successRoot);

                    if (isFullScreen) {
                        Platform.runLater(() -> {
                            stage.setFullScreen(true);
                            stage.setFullScreenExitHint("");
                        });
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Lỗi");
                    error.setHeaderText("Không thể chuyển trang");
                    error.setContentText("Vui lòng thử lại.");
                    error.showAndWait();
                }
            });

        }));
        checkPaymentTimeline.play();
    }

    private void handlePaymentExpired() {
        currentBooking.setPaymentStatus(PaymentStatus.EXPIRED);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hết thời gian thanh toán");
        alert.setHeaderText("⏰ Đã hết thời gian thanh toán!");
        alert.setContentText(
                "Đơn hàng của bạn đã bị hủy do quá thời gian thanh toán.\n\n" +
                        "Vui lòng đặt vé lại nếu muốn tiếp tục.");

        confirmPaymentButton.setDisable(true);
        confirmPaymentButton.setText("Đã hết hạn");

        alert.showAndWait();

        System.out.println("Quay về trang chủ...");
        // TODO: Chuyển về trang chủ hoặc trang chọn phim
    }
}