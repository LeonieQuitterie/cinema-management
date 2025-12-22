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

// ✅ THÊM: Socket.IO imports
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;

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

    // ✅ THÊM: Socket.IO connection
    private Socket paymentSocket;
    private Timeline pollingTimeline; // Backup polling

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("PaymentController initialized");
        System.out.println("⏳ Waiting for booking data...");
        // ❌ XÓA: Không dùng mock data nữa
        // currentBooking = createMockBooking();
        // loadBookingData();
        // startCountdown();
    }

    // ✅ THÊM: Setter để nhận booking từ BookingConfirmationController
    public void setBooking(Booking booking) {
        this.currentBooking = booking;

        System.out.println("=== PaymentController.setBooking() ===");
        System.out.println("📌 Received booking: " + booking.getId());
        System.out.println("📌 Total price: " + booking.getTotalPrice());
        System.out.println("📌 Cinema: " + booking.getCinemaName());
        System.out.println("📌 Movie: " + booking.getMovieTitle());

        // ✅ Load data và start countdown
        Platform.runLater(() -> {
            loadBookingData();
            startCountdown();
            connectPaymentSocket(); // ✅ Kết nối Socket.IO
        });
    }

    // ✅ THÊM: Kết nối Socket.IO để nhận payment updates
    private void connectPaymentSocket() {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[] { "websocket" };
            options.reconnection = true;
            options.reconnectionAttempts = 5;
            options.reconnectionDelay = 1000;

            paymentSocket = IO.socket("http://localhost:3000/payment", options);

            // ✅ Lắng nghe event payment status từ server
            paymentSocket.on("payment:status", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String status = data.getString("status");
                    String bookingId = data.getString("bookingId");

                    System.out.println("💰 Payment status received: " + status + " for " + bookingId);

                    if ("SUCCESS".equals(status) && bookingId.equals(currentBooking.getId())) {
                        Platform.runLater(() -> handlePaymentSuccess(data));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing payment status: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            paymentSocket.on(Socket.EVENT_CONNECT, args -> {
                System.out.println("✅ Connected to payment socket");

                // Join room của booking này
                paymentSocket.emit("join-booking", currentBooking.getId());
                System.out.println("📌 Joined booking room: " + currentBooking.getId());
            });

            paymentSocket.on(Socket.EVENT_DISCONNECT, args -> {
                System.out.println("❌ Disconnected from payment socket");
                // Start polling backup
                if (pollingTimeline == null && remainingSeconds > 0) {
                    startPaymentPolling();
                }
            });

            paymentSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                System.err.println("⚠️ Payment socket connection error: " + args[0]);
                // Start polling backup
                if (pollingTimeline == null && remainingSeconds > 0) {
                    startPaymentPolling();
                }
            });

            paymentSocket.connect();

        } catch (Exception e) {
            System.err.println("Failed to initialize payment socket: " + e.getMessage());
            e.printStackTrace();
            // Fallback to polling
            startPaymentPolling();
        }
    }

    // ✅ THÊM: Polling backup nếu Socket fail
    private void startPaymentPolling() {
        System.out.println("🔄 Starting payment polling backup...");

        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            checkPaymentStatus();
        }));

        pollingTimeline.setCycleCount(180); // 15 phút = 180 * 5s
        pollingTimeline.play();
    }

    // ✅ THÊM: Check payment status qua API
    private void checkPaymentStatus() {
        new Thread(() -> {
            try {
                // TODO: Call API /api/payment/status/{bookingId}
                // Sử dụng BookingApiClient hoặc HTTP client
                // Nếu paid → Platform.runLater(() -> handlePaymentSuccess(...))

                System.out.println("🔍 Polling payment status for " + currentBooking.getId());

            } catch (Exception e) {
                System.err.println("Error polling payment status: " + e.getMessage());
            }
        }).start();
    }

    // ✅ THÊM: Xử lý khi nhận được payment success
    private void handlePaymentSuccess(JSONObject data) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }

        currentBooking.setPaymentStatus(PaymentStatus.PAID);

        try {
            int amount = data.getInt("amount");
            String transactionId = data.optString("transactionId", "N/A");

            System.out.println("✅ Payment confirmed:");
            System.out.println("   Amount: " + amount);
            System.out.println("   Transaction ID: " + transactionId);

        } catch (Exception e) {
            System.err.println("Error extracting payment details: " + e.getMessage());
        }

        // Hiển thị thông báo thành công
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thanh toán thành công");
        alert.setHeaderText("✅ Đã nhận được thanh toán!");
        alert.setContentText(
                "Giao dịch của bạn đã được xác nhận.\n\n" +
                        "Mã booking: " + currentBooking.getId() + "\n" +
                        "Số tiền: " + currentBooking.getFormattedTotalPrice() + "\n\n" +
                        "Chúng tôi sẽ chuyển bạn đến trang xác nhận.");

        alert.showAndWait();

        // Chuyển sang booking success page
        navigateToBookingSuccess();
    }

    private void navigateToBookingSuccess() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cinema/booking-success.fxml"));
            Parent successRoot = loader.load();

            // TODO: Pass booking data to success controller nếu cần
            // BookingSuccessController controller = loader.getController();
            // controller.setBooking(currentBooking);

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

        cleanup();
        alert.showAndWait();

        System.out.println("Quay về trang chủ...");
        // TODO: Chuyển về trang chủ hoặc trang chọn phim
    }

    public void cleanup() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }
        if (paymentSocket != null && paymentSocket.connected()) {
            paymentSocket.disconnect();
            System.out.println("🔌 Payment socket disconnected");
        }
    }
}