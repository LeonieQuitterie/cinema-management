package com.cinema.models;

public class ScreenView {

    private String id;
    private String name;        // "Phòng 1", "Phòng 2"
    private String cinemaId;

    // GỘP TỪ SHOWTIME (API / UI only)
    private String timeRange;   // "15:30 - 17:45"
    private int availableSeats; // 98
    private int totalSeats;     // 142

    public ScreenView() {
    }

    public ScreenView(
            String id,
            String name,
            String cinemaId,
            String timeRange,
            int availableSeats,
            int totalSeats
    ) {
        this.id = id;
        this.name = name;
        this.cinemaId = cinemaId;
        this.timeRange = timeRange;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
    }

    // ===== Getters & Setters =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
}
