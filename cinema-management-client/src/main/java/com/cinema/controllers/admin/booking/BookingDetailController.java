package com.cinema.controllers.admin.booking;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.NumberFormat;

public class BookingDetailController implements Initializable {
    
    // Header
    @FXML private Label bookingIdLabel;
    @FXML private Label bookingTimeLabel;
    @FXML private Label statusBadge;
    
    // Movie Info
    @FXML private ImageView moviePosterImage;
    @FXML private Label movieTitleLabel;
    @FXML private Label cinemaLabel;
    @FXML private Label screenLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label formatLabel;
    @FXML private Label ageRatingLabel;
    
    // Customer Info
    @FXML private Label customerNameLabel;
    @FXML private Label customerPhoneLabel;
    @FXML private Label customerEmailLabel;
    
    // Seats Info
    @FXML private Label seatsLabel;
    @FXML private Label seatPriceLabel;
    
    // Combos Info
    @FXML private VBox combosContainer;
    @FXML private Label comboPriceLabel;
    
    // Payment Info
    @FXML private Label paymentStatusLabel;
    @FXML private Label paymentDeadlineLabel;
    @FXML private Label bankNameLabel;
    @FXML private Label accountNumberLabel;
    @FXML private Label totalPriceLabel;
    
    // Action Buttons
    @FXML private Button confirmPaymentBtn;
    @FXML private Button refundBtn;
    @FXML private Button cancelBtn;
    
    private NumberFormat currencyFormat;
    private BookingListController.BookingRow currentBooking;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    }
    
    public void setBooking(BookingListController.BookingRow booking) {
        this.currentBooking = booking;
        loadBookingDetails();
    }
    
    private void loadBookingDetails() {
        if (currentBooking == null) return;
        
        // Header
        bookingIdLabel.setText("ĐƠN HÀNG #" + currentBooking.getBookingId());
        bookingTimeLabel.setText("Đặt lúc: " + currentBooking.getBookingTime());
        
        // Status badge
        statusBadge.setText(currentBooking.getStatus());
        statusBadge.getStyleClass().clear();
        statusBadge.getStyleClass().add("status-badge");
        
        String status = currentBooking.getStatus();
        if (status.contains("Đã thanh toán")) {
            statusBadge.setStyle("-fx-background-color: #4caf50;");
            confirmPaymentBtn.setVisible(false);
            refundBtn.setVisible(true);
            cancelBtn.setVisible(false);
        } else if (status.contains("Chờ")) {
            statusBadge.setStyle("-fx-background-color: #ff9800;");
            confirmPaymentBtn.setVisible(true);
            refundBtn.setVisible(false);
            cancelBtn.setVisible(true);
        } else if (status.contains("Hết hạn")) {
            statusBadge.setStyle("-fx-background-color: #f44336;");
            confirmPaymentBtn.setVisible(false);
            refundBtn.setVisible(false);
            cancelBtn.setVisible(false);
        } else if (status.contains("Đã hủy")) {
            statusBadge.setStyle("-fx-background-color: #9e9e9e;");
            confirmPaymentBtn.setVisible(false);
            refundBtn.setVisible(false);
            cancelBtn.setVisible(false);
        }
        
        // Movie Info
        movieTitleLabel.setText(currentBooking.getMovieTitle());
        cinemaLabel.setText(currentBooking.getCinemaName());
        screenLabel.setText(currentBooking.getScreenName());
        showtimeLabel.setText(currentBooking.getShowtime());
        formatLabel.setText("2D Phụ đề"); // TODO: Get from actual booking
        ageRatingLabel.setText("C13"); // TODO: Get from actual booking
        
        // Customer Info
        customerNameLabel.setText(currentBooking.getCustomerName());
        customerPhoneLabel.setText(currentBooking.getPhone());
        customerEmailLabel.setText("customer@email.com"); // TODO: Get from actual booking
        
        // Seats Info
        seatsLabel.setText(currentBooking.getSeats());
        double seatPrice = parsePrice(currentBooking.getTotalPrice()) * 0.7; // Assume 70% is seat price
        seatPriceLabel.setText(formatCurrency(seatPrice));
        
        // Combos Info - TODO: Load actual combos
        loadCombos();
        double comboPrice = parsePrice(currentBooking.getTotalPrice()) * 0.3; // Assume 30% is combo
        comboPriceLabel.setText(formatCurrency(comboPrice));
        
        // Payment Info
        paymentStatusLabel.setText(currentBooking.getStatus());
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        LocalDateTime deadline = LocalDateTime.now().plusMinutes(15); // Mock deadline
        paymentDeadlineLabel.setText(deadline.format(dtf));
        
        bankNameLabel.setText("Vietcombank"); // TODO: Get from actual payment info
        accountNumberLabel.setText("1234567890"); // TODO: Get from actual payment info
        
        // Total Price
        totalPriceLabel.setText(currentBooking.getTotalPrice());
        
        // TODO: Load movie poster image
        // moviePosterImage.setImage(new Image(posterUrl));
    }
    
    private void loadCombos() {
        combosContainer.getChildren().clear();
        
        // TODO: Load actual combos from booking
        // Mock data
        Label combo1 = new Label("• Combo Couple Sweet x1 - 120.000đ");
        combo1.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        Label combo2 = new Label("• Bắp rang bơ lớn x2 - 130.000đ");
        combo2.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        combosContainer.getChildren().addAll(combo1, combo2);
    }
    
    @FXML
    private void handlePrintTicket() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("In vé");
        alert.setHeaderText("In vé đơn hàng " + currentBooking.getBookingId());
        alert.setContentText("Đang tiến hành in vé...");
        alert.showAndWait();
        
        // TODO: Implement actual print functionality
    }
    
    @FXML
    private void handleSendEmail() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gửi email");
        alert.setHeaderText("Gửi email xác nhận");
        alert.setContentText("Email đã được gửi đến " + customerEmailLabel.getText());
        alert.showAndWait();
        
        // TODO: Implement actual email sending
    }
    
    @FXML
    private void handleConfirmPayment() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Xác nhận đơn hàng " + currentBooking.getBookingId());
        confirm.setContentText("Bạn có chắc chắn đã nhận được thanh toán?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Update booking status to PAID
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Thành công");
            success.setHeaderText(null);
            success.setContentText("Đã xác nhận thanh toán thành công!");
            success.showAndWait();
            
            closeModal();
        }
    }
    
    @FXML
    private void handleRefund() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hoàn tiền");
        dialog.setHeaderText("Hoàn tiền đơn hàng " + currentBooking.getBookingId());
        dialog.setContentText("Nhập lý do hoàn tiền:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            // TODO: Process refund
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận hoàn tiền");
            confirm.setHeaderText("Hoàn tiền: " + totalPriceLabel.getText());
            confirm.setContentText("Lý do: " + reason + "\n\nXác nhận hoàn tiền?");
            
            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Thành công");
                success.setHeaderText(null);
                success.setContentText("Đã hoàn tiền thành công!");
                success.showAndWait();
                
                closeModal();
            }
        });
    }
    
    @FXML
    private void handleCancel() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hủy vé");
        dialog.setHeaderText("Hủy đơn hàng " + currentBooking.getBookingId());
        dialog.setContentText("Nhập lý do hủy:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận hủy vé");
            confirm.setHeaderText("Hủy đơn hàng " + currentBooking.getBookingId());
            confirm.setContentText("Lý do: " + reason + "\n\nXác nhận hủy vé?");
            
            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                // TODO: Cancel booking
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Thành công");
                success.setHeaderText(null);
                success.setContentText("Đã hủy vé thành công!");
                success.showAndWait();
                
                closeModal();
            }
        });
    }
    
    @FXML
    private void handleClose() {
        closeModal();
    }
    
    private void closeModal() {
        Stage stage = (Stage) bookingIdLabel.getScene().getWindow();
        stage.close();
    }
    
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount) + "đ";
    }
    
    private double parsePrice(String priceString) {
        String numberOnly = priceString.replace("đ", "").replace(".", "").replace(",", "").trim();
        try {
            return Double.parseDouble(numberOnly);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}