package com.cinema.models;

import java.util.ArrayList;
import java.util.List;

public class Screen {
    private String id;
    private String name; // VD: "Phòng 1", "Screen A"
    private String cinemaId;
    private SeatLayout seatLayout; // Sơ đồ ghế của phòng này
    private int totalSeats;

    private List<Showtime> showtimes = new ArrayList<>(); // THÊM DÒNG NÀY

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    public Screen() {
    }

    public Screen(String id, String name, String cinemaId, SeatLayout seatLayout) {
        this.id = id;
        this.name = name;
        this.cinemaId = cinemaId;
        this.seatLayout = seatLayout;
        this.totalSeats = calculateTotalSeats();
    }

    private int calculateTotalSeats() {
        if (seatLayout == null || seatLayout.getSeats() == null)
            return 0;
        int count = 0;
        for (List<Seat> row : seatLayout.getSeats()) {
            for (Seat seat : row) {
                if (seat != null && seat.getSeatNumber() != null && !seat.getSeatNumber().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Getters and Setters
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

    public SeatLayout getSeatLayout() {
        return seatLayout;
    }

    public void setSeatLayout(SeatLayout seatLayout) {
        this.seatLayout = seatLayout;
        this.totalSeats = calculateTotalSeats();
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
}