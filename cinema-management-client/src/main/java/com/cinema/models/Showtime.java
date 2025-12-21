package com.cinema.models;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Showtime {
    private String id;
    private String movieId;
    private String screenId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double basePrice;
    private String format; // 2D, 3D, IMAX...
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

    // ================== GETTERS & SETTERS ==================

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

    public List<String> getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(List<String> bookedSeats) {
        this.bookedSeats = (bookedSeats != null) ? bookedSeats : new ArrayList<>();
    }
}