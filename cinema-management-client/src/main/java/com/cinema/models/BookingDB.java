package com.cinema.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.google.gson.annotations.SerializedName;

/**
 * Booking model
 * Đồng bộ 1–1 với bảng BOOKINGS trong CSDL
 */
public class BookingDB {

    // ================== CORE IDs ==================
    @SerializedName("id")
    private String id;

    @SerializedName("movie_id")
    private String movieId;

    @SerializedName("cinema_id")
    private String cinemaId;

    @SerializedName("screen_id")
    private String screenId;

    @SerializedName("showtime_id")
    private String showtimeId;

    @SerializedName("customer_id")
    private String customerId;

    // ================== PRICES ==================
    @SerializedName("seat_total_price")
    private double seatTotalPrice;

    @SerializedName("combo_total_price")
    private double comboTotalPrice;

    @SerializedName("total_price")
    private double totalPrice;

    // ================== PAYMENT ==================
    @SerializedName("payment_status")
    private PaymentStatus paymentStatus;

    @SerializedName("booking_time")
    private LocalDateTime bookingTime;

    @SerializedName("payment_deadline")
    private LocalDateTime paymentDeadline;

    // ================== BANK INFO ==================
    @SerializedName("bank_name")
    private String bankName;

    @SerializedName("account_holder")
    private String accountHolder;

    @SerializedName("account_number")
    private String accountNumber;

    @SerializedName("transfer_content")
    private String transferContent;

    @SerializedName("qr_code_url")
    private String qrCodeUrl;

    // ================== CLIENT / UI ONLY ==================
    // ❗ Không map DB, chỉ dùng hiển thị
    private String movieTitle;
    private String moviePosterUrl;
    private String ageRating;
    private String ageRatingDescription;

    private String cinemaName;
    private String cinemaLogoUrl;

    private String screenName;
    private LocalDateTime showtime;
    private String format;

    private List<String> selectedSeats;
    private List<ComboOrderItem> combos;

    private Customer customer;
    private PaymentInfo paymentInfo;

    // ================== CONSTRUCTOR ==================
    public BookingDB() {
        this.paymentStatus = PaymentStatus.PENDING;
    }
    public BookingDB(Booking booking) {
        this.id = booking.getId();
        this.movieId = booking.getMovieId();
        this.cinemaId = booking.getCinemaId();
        // this.screenId = booking.getScreenId();
        // this.showtimeId = booking.getShowtimeId();
        // this.customerId = booking.getCustomerId();
        this.seatTotalPrice = booking.getSeatTotalPrice();
        this.comboTotalPrice = booking.getComboTotalPrice();
        this.totalPrice = booking.getTotalPrice();
        // this.paymentStatus = booking.getPaymentStatus().name();
        this.bookingTime = booking.getBookingTime();
        this.paymentDeadline = booking.getPaymentDeadline();

        if (booking.getPaymentInfo() != null) {
            this.bankName = booking.getPaymentInfo().getBankName();
            // this.accountHolder = booking.getPaymentInfo().getBankAccountHolder();
            // this.accountNumber = booking.getPaymentInfo().getBankAccountNumber();
            this.transferContent = booking.getPaymentInfo().getTransferContent();
            this.qrCodeUrl = booking.getPaymentInfo().getQrCodeUrl();
        }
    }

    // ================== GETTERS & SETTERS ==================
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getCinemaId() { return cinemaId; }
    public void setCinemaId(String cinemaId) { this.cinemaId = cinemaId; }

    public String getScreenId() { return screenId; }
    public void setScreenId(String screenId) { this.screenId = screenId; }

    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public double getSeatTotalPrice() { return seatTotalPrice; }
    public void setSeatTotalPrice(double seatTotalPrice) { this.seatTotalPrice = seatTotalPrice; }

    public double getComboTotalPrice() { return comboTotalPrice; }
    public void setComboTotalPrice(double comboTotalPrice) { this.comboTotalPrice = comboTotalPrice; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public LocalDateTime getPaymentDeadline() { return paymentDeadline; }
    public void setPaymentDeadline(LocalDateTime paymentDeadline) { this.paymentDeadline = paymentDeadline; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getTransferContent() { return transferContent; }
    public void setTransferContent(String transferContent) { this.transferContent = transferContent; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    // ===== UI fields =====
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getMoviePosterUrl() { return moviePosterUrl; }
    public void setMoviePosterUrl(String moviePosterUrl) { this.moviePosterUrl = moviePosterUrl; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public String getAgeRatingDescription() { return ageRatingDescription; }
    public void setAgeRatingDescription(String ageRatingDescription) {
        this.ageRatingDescription = ageRatingDescription;
    }

    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }

    public String getCinemaLogoUrl() { return cinemaLogoUrl; }
    public void setCinemaLogoUrl(String cinemaLogoUrl) { this.cinemaLogoUrl = cinemaLogoUrl; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public LocalDateTime getShowtime() { return showtime; }
    public void setShowtime(LocalDateTime showtime) { this.showtime = showtime; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public List<String> getSelectedSeats() { return selectedSeats; }
    public void setSelectedSeats(List<String> selectedSeats) { this.selectedSeats = selectedSeats; }

    public List<ComboOrderItem> getCombos() { return combos; }
    public void setCombos(List<ComboOrderItem> combos) { this.combos = combos; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public PaymentInfo getPaymentInfo() { return paymentInfo; }
    public void setPaymentInfo(PaymentInfo paymentInfo) { this.paymentInfo = paymentInfo; }

    // ================== HELPER METHODS ==================
    public String getFormattedTotalPrice() {
        return String.format("%,.0f đ", totalPrice);
    }

    public String getFormattedShowtime() {
        if (showtime == null) return "";
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy – HH:mm", new Locale("vi", "VN"));
        return showtime.format(fmt);
    }
}
