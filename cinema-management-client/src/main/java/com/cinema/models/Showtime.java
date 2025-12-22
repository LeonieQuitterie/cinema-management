package com.cinema.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.List;

public class Showtime {
    private String id;
    
    @SerializedName("movie_id")
    private String movieId;
    
    @SerializedName("screen_id")
    private String screenId;
    
    @SerializedName("start_time")
    private LocalDateTime startTime;
    
    @SerializedName("end_time")
    private LocalDateTime endTime;
    
    @SerializedName("base_price")
    private double basePrice;
    
    private String format;
    
    // Additional fields from JOIN
    @SerializedName("movie_title")
    private String movieTitle;
    
    @SerializedName("movie_duration")
    private Integer movieDuration;
    
    @SerializedName("screen_name")
    private String screenName;
    
    @SerializedName("total_seats")
    private Integer totalSeats;
    
    @SerializedName("cinema_id")
    private String cinemaId;
    
    @SerializedName("cinema_name")
    private String cinemaName;
    
    @SerializedName("booked_seats_count")
    private Integer bookedSeatsCount;
    
    @SerializedName("available_seats")
    private Integer availableSeats;
    
    @SerializedName("poster_url")
    private String posterUrl;
    
    // For client use
    private List<String> bookedSeats;
    
    // Constructors
    public Showtime() {}

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
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    
    public Integer getMovieDuration() { return movieDuration; }
    public void setMovieDuration(Integer movieDuration) { this.movieDuration = movieDuration; }
    
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    
    public String getCinemaId() { return cinemaId; }
    public void setCinemaId(String cinemaId) { this.cinemaId = cinemaId; }
    
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
    
    public Integer getBookedSeatsCount() { return bookedSeatsCount; }
    public void setBookedSeatsCount(Integer bookedSeatsCount) { this.bookedSeatsCount = bookedSeatsCount; }
    
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    
    public List<String> getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(List<String> bookedSeats) { this.bookedSeats = bookedSeats; }
}