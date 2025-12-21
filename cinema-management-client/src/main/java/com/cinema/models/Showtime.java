package com.cinema.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Showtime {
    private String id;
    private String movieId;
    private String screenId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double basePrice;              // Giá vé cơ bản
    private List<String> bookedSeats;      // Danh sách số ghế đã đặt
    
    public Showtime() {
        this.bookedSeats = new ArrayList<>();
    }
    
    public Showtime(String id, String movieId, String screenId, LocalDateTime startTime, 
                    LocalDateTime endTime, double basePrice) {
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.bookedSeats = new ArrayList<>();
    }
    
    // Kiểm tra ghế đã được đặt chưa
    public boolean isSeatBooked(String seatNumber) {
        return bookedSeats.contains(seatNumber);
    }
    
    // Đặt ghế
    public void bookSeat(String seatNumber) {
        if (!bookedSeats.contains(seatNumber)) {
            bookedSeats.add(seatNumber);
        }
    }
    
    // Hủy đặt ghế
    public void unbookSeat(String seatNumber) {
        bookedSeats.remove(seatNumber);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    
    public String getScreenId() { return screenId; }
    public void setScreenId(String screenId) { this.screenId = screenId; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    
    public List<String> getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(List<String> bookedSeats) { this.bookedSeats = bookedSeats; }
}