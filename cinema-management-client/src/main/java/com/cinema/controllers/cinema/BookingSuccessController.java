package com.cinema.controllers.cinema;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.cinema.models.*;

public class BookingSuccessController implements Initializable {

    @FXML private Label bookingCodeLabel;
    @FXML private ImageView ticketQrImage;
    @FXML private ImageView moviePosterImage;
    
    @FXML private Label movieTitleLabel;
    @FXML private Label cinemaLabel;
    @FXML private Label screenLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label seatsLabel;
    @FXML private Label formatLabel;
    @FXML private Label combosLabel;
    
    @FXML private Label customerNameLabel;
    @FXML private Label customerPhoneLabel;
    @FXML private Label customerEmailLabel;
    
    @FXML private Label totalPriceLabel;
    
    @FXML private Button addToCalendarButton;
    @FXML private Button downloadTicketButton;
    @FXML private Button backToHomeButton;

    private Booking successBooking;

    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Tạo mock data cho booking thành công
        successBooking = createSuccessBooking();
        
        // Load dữ liệu lên UI
        loadBookingSuccessData();
    }

    private Booking createSuccessBooking() {
        Booking booking = new Booking();
        
        // Thông tin cơ bản
        booking.setId("BOOK20251209001");
        booking.setMovieId("M001");
        booking.setMovieTitle("Võ Sĩ Giác Đấu II");
        booking.setMoviePosterUrl("https://i.pinimg.com/736x/7b/5a/57/7b5a578a9b1d18a0f3fda1b9b3c39f50.jpg");
        booking.setAgeRating("C18");
        booking.setAgeRatingDescription("Phim dành cho khán giả từ đủ 18 tuổi trở lên");
        
        // Thông tin rạp
        booking.setCinemaId("C001");
        booking.setCinemaName("CGV Vincom Đà Nẵng");
        booking.setCinemaLogoUrl("https://i.pinimg.com/736x/7b/5a/57/7b5a578a9b1d18a0f3fda1b9b3c39f50.jpg");
        booking.setScreenName("Cinema 4");
        
        // Thời gian
        LocalDateTime showtime = LocalDateTime.of(2025, 12, 9, 20, 20);
        booking.setShowtime(showtime);
        booking.setFormat("2D Phụ đề");
        booking.setBookingTime(LocalDateTime.now());
        
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
        customer.setFullName("Nguyễn Văn An");
        customer.setPhoneNumber("0901234567");
        customer.setEmail("nguyenvanan@gmail.com");
        booking.setCustomer(customer);
        
        // Tổng tiền
        booking.setTotalPrice(405000);
        
        // Đã thanh toán thành công
        booking.setPaymentStatus(PaymentStatus.PAID);
        
        return booking;
    }

    private void loadBookingSuccessData() {
        // Mã đặt vé
        bookingCodeLabel.setText(successBooking.getId());
        
        // Load QR code vé điện tử
        try {
            String qrUrl = successBooking.getTicketQrCodeUrl();
            Image qrImage = new Image(qrUrl, true);
            ticketQrImage.setImage(qrImage);
        } catch (Exception e) {
            System.err.println("Không thể load QR vé: " + e.getMessage());
        }
        
        // Load poster phim
        try {
            Image posterImage = new Image(successBooking.getMoviePosterUrl(), true);
            moviePosterImage.setImage(posterImage);
        } catch (Exception e) {
            System.err.println("Không thể load poster: " + e.getMessage());
        }
        
        // Thông tin phim và vé
        movieTitleLabel.setText(successBooking.getMovieTitle());
        cinemaLabel.setText(successBooking.getCinemaName());
        screenLabel.setText(successBooking.getScreenName());
        
        // Format showtime với endtime
        showtimeLabel.setText(successBooking.getFullShowtimeRange());
        
        seatsLabel.setText(successBooking.getSelectedSeatsString());
        formatLabel.setText(successBooking.getFormat());
        combosLabel.setText(successBooking.getCombosDescription());
        
        // Thông tin khách hàng
        Customer customer = successBooking.getCustomer();
        if (customer != null) {
            customerNameLabel.setText(customer.getFullName());
            customerPhoneLabel.setText(successBooking.getFormattedPhoneNumber());
            customerEmailLabel.setText(customer.getEmail());
        }
        
        // Tổng tiền
        totalPriceLabel.setText(successBooking.getFormattedTotalPrice());
    }

    @FXML
    private void handleAddToCalendar() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thêm vào lịch");
        alert.setHeaderText("📅 Thêm vào lịch");
        
        // Tạo thông tin sự kiện
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy lúc HH:mm", new Locale("vi", "VN"));
        String eventTime = successBooking.getShowtime().format(formatter);
        
        String eventDetails = String.format(
            "Sự kiện: Xem phim %s\n" +
            "Thời gian: %s\n" +
            "Địa điểm: %s - %s\n" +
            "Ghế: %s\n\n" +
            "Mã đặt vé: %s",
            successBooking.getMovieTitle(),
            eventTime,
            successBooking.getCinemaName(),
            successBooking.getScreenName(),
            successBooking.getSelectedSeatsString(),
            successBooking.getId()
        );
        
        alert.setContentText(eventDetails + "\n\n✅ Đã thêm vào lịch Google Calendar / Apple Calendar!");
        alert.showAndWait();
        
        // TODO: Tích hợp API Google Calendar hoặc tạo file .ics
        System.out.println("Thêm vào lịch: " + successBooking.getId());
    }

    @FXML
    private void handleDownloadTicket() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tải vé");
        alert.setHeaderText("📥 Đang tải vé điện tử...");
        alert.setContentText(
            "Vé điện tử của bạn đã được tải xuống!\n\n" +
            "Định dạng: PDF với mã QR\n" +
            "Lưu tại: Downloads/CinemaPro_" + successBooking.getId() + ".pdf\n\n" +
            "Bạn có thể in vé này hoặc xuất trình bản điện tử tại rạp."
        );
        alert.showAndWait();
        
        // TODO: Sinh PDF hoặc ảnh vé với QR code
        System.out.println("Tải vé: " + successBooking.getId());
    }

    @FXML
    private void handleBackToHome() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Về trang chủ");
        alert.setHeaderText("Cảm ơn bạn đã sử dụng dịch vụ!");
        alert.setContentText(
            "Chúc bạn có trải nghiệm xem phim tuyệt vời! 🎬\n\n" +
            "Hẹn gặp lại bạn trong những buổi chiếu tiếp theo."
        );
        alert.showAndWait();
        
        System.out.println("Quay về trang chủ...");
        // TODO: Chuyển về trang chủ hoặc trang danh sách phim
    }

    









    
}