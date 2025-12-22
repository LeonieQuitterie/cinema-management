// src/main/java/com/cinema/models/dto/BookingResponseDTO.java
package com.cinema.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class BookingResponseDTO {
    @JsonProperty("bookingId")
    private String bookingId;
    
    @JsonProperty("transferContent")
    private String transferContent;
    
    @JsonProperty("paymentDeadline")
    private LocalDateTime paymentDeadline;
    
    @JsonProperty("movieId")
    private String movieId;
    
    @JsonProperty("cinemaId")
    private String cinemaId;
    
    @JsonProperty("screenId")
    private String screenId;
    
    @JsonProperty("showtimeId")
    private String showtimeId;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("seatTotalPrice")
    private double seatTotalPrice;
    
    @JsonProperty("comboTotalPrice")
    private double comboTotalPrice;
    
    @JsonProperty("totalPrice")
    private double totalPrice;

    // Constructors
    public BookingResponseDTO() {
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTransferContent() {
        return transferContent;
    }

    public void setTransferContent(String transferContent) {
        this.transferContent = transferContent;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getSeatTotalPrice() {
        return seatTotalPrice;
    }

    public void setSeatTotalPrice(double seatTotalPrice) {
        this.seatTotalPrice = seatTotalPrice;
    }

    public double getComboTotalPrice() {
        return comboTotalPrice;
    }

    public void setComboTotalPrice(double comboTotalPrice) {
        this.comboTotalPrice = comboTotalPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "BookingResponseDTO{" +
                "bookingId='" + bookingId + '\'' +
                ", transferContent='" + transferContent + '\'' +
                ", paymentDeadline=" + paymentDeadline +
                ", totalPrice=" + totalPrice +
                '}';
    }
}