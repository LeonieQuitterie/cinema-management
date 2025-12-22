package com.cinema.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.google.gson.annotations.SerializedName;

public class Booking {
    private String id;
    @SerializedName("movie_id")
    
    private String movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private String ageRating;
    private String ageRatingDescription;
   
    private String cinemaId;
    private String cinemaName;
    private String cinemaLogoUrl;
   
    private String screenName;
    private LocalDateTime showtime;
    private String format; // "2D phụ đề", "3D lồng tiếng"
   
    private List<String> selectedSeats;
    private double seatTotalPrice;
   
    private List<ComboOrderItem> combos;
    private double comboTotalPrice;
   
    private Customer customer;
   
    private double totalPrice;
    private LocalDateTime bookingTime;
    
    // THÊM MỚI CHO PAYMENT
    private PaymentStatus paymentStatus;
    private PaymentInfo paymentInfo;
    private LocalDateTime paymentDeadline; // Thời hạn thanh toán (15 phút)

    public Booking() {
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = moviePosterUrl;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getAgeRatingDescription() {
        return ageRatingDescription;
    }

    public void setAgeRatingDescription(String ageRatingDescription) {
        this.ageRatingDescription = ageRatingDescription;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getCinemaLogoUrl() {
        return cinemaLogoUrl;
    }

    public void setCinemaLogoUrl(String cinemaLogoUrl) {
        this.cinemaLogoUrl = cinemaLogoUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public LocalDateTime getShowtime() {
        return showtime;
    }

    public void setShowtime(LocalDateTime showtime) {
        this.showtime = showtime;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(List<String> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    public double getSeatTotalPrice() {
        return seatTotalPrice;
    }

    public void setSeatTotalPrice(double seatTotalPrice) {
        this.seatTotalPrice = seatTotalPrice;
    }

    public List<ComboOrderItem> getCombos() {
        return combos;
    }

    public void setCombos(List<ComboOrderItem> combos) {
        this.combos = combos;
    }

    public double getComboTotalPrice() {
        return comboTotalPrice;
    }

    public void setComboTotalPrice(double comboTotalPrice) {
        this.comboTotalPrice = comboTotalPrice;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    // Helper methods
    public String getFormattedShowtime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy – HH:mm", new Locale("vi", "VN"));
        return showtime.format(formatter);
    }

    public String getSelectedSeatsString() {
        return String.join(", ", selectedSeats);
    }

    public String getFormattedTotalPrice() {
        return String.format("%,.0f đ", totalPrice);
    }

    public String getCombosDescription() {
        if (combos == null || combos.isEmpty()) {
            return "Không có";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < combos.size(); i++) {
            ComboOrderItem item = combos.get(i);
            sb.append(item.getFoodCombo().getName()).append(" ×").append(item.getQuantity());
            if (i < combos.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    // Thêm các helper methods cho trang thành công
    public String getTicketQrCodeUrl() {
        // URL hoặc path đến mã QR vé điện tử (khác với QR thanh toán)
        return "https://via.placeholder.com/500x500/ffffff/000000?text=TICKET+" + id;
    }

    public LocalDateTime getEndTime() {
        // Tính thời gian kết thúc dựa vào showtime (cần có thông tin duration từ Movie)
        // Giả sử duration mặc định là 120 phút nếu không có
        return showtime != null ? showtime.plusMinutes(120) : null;
    }

    public String getFullShowtimeRange() {
        if (showtime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy – HH:mm", new Locale("vi", "VN"));
        String start = showtime.format(formatter);
        
        LocalDateTime end = getEndTime();
        if (end != null) {
            String endTime = end.format(DateTimeFormatter.ofPattern("HH:mm"));
            return start + " ~ " + endTime;
        }
        return start;
    }

    public String getFormattedPhoneNumber() {
        if (customer == null || customer.getPhoneNumber() == null) return "";
        String phone = customer.getPhoneNumber();
        // Format: 0901 234 567
        if (phone.length() == 10) {
            return phone.substring(0, 4) + " " + phone.substring(4, 7) + " " + phone.substring(7);
        }
        return phone;
    }
}