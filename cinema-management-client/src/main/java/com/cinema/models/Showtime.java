package com.cinema.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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

    public boolean isSeatBooked(String seatNumber) {
        return bookedSeats.contains(seatNumber);
    }

    public void bookSeat(String seatNumber) {
        if (!bookedSeats.contains(seatNumber)) {
            bookedSeats.add(seatNumber);
        }
    }

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

    /**
     * Parse từ String dạng ISO 8601 có Z (UTC): "2025-12-26T12:00:00.000Z"
     * → Convert đúng sang giờ Việt Nam (UTC+7)
     */
    public void setStartTime(String startTimeStr) {
        if (startTimeStr == null || startTimeStr.isEmpty()) {
            this.startTime = null;
            return;
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(startTimeStr);
            // Convert UTC → giờ Việt Nam (Asia/Ho_Chi_Minh)
            this.startTime = odt.atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))
                               .toLocalDateTime();

            System.out.println("Parsed startTime: " + startTimeStr + " → " + this.startTime + " (VN)");
        } catch (Exception e) {
            System.err.println("Error parsing startTime: " + startTimeStr);
            e.printStackTrace();
            this.startTime = null;
        }
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    /**
     * Tương tự cho endTime
     */
    public void setEndTime(String endTimeStr) {
        if (endTimeStr == null || endTimeStr.isEmpty()) {
            this.endTime = null;
            return;
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(endTimeStr);
            this.endTime = odt.atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))
                             .toLocalDateTime();

            System.out.println("Parsed endTime: " + endTimeStr + " → " + this.endTime + " (VN)");
        } catch (Exception e) {
            System.err.println("Error parsing endTime: " + endTimeStr);
            e.printStackTrace();
            this.endTime = null;
        }
    }

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
    public void setBookedSeats(List<String> bookedSeats) {
        this.bookedSeats = (bookedSeats != null) ? bookedSeats : new ArrayList<>();
    }
}