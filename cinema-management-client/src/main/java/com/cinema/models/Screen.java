package com.cinema.models;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Screen {
    private String id;
    private String name; // VD: "Phòng 1", "Screen A"

    @SerializedName("cinema_id")
    private String cinemaId;
    
    @SerializedName("row_count")
    private Integer rowCount;
    
    @SerializedName("column_count")
    private Integer columnCount;
    
    @SerializedName("total_seats")
    private Integer totalSeats;
    
    @SerializedName("seat_layout")
    private SeatLayout seatLayout;

    public Screen() {}

    public Screen(String id, String name, String cinemaId, SeatLayout seatLayout) {
        this.id = id;
        this.name = name;
        this.cinemaId = cinemaId;
        this.seatLayout = seatLayout;
        this.totalSeats = calculateTotalSeats();
    }

    private int calculateTotalSeats() {
        if (seatLayout == null || seatLayout.getSeats() == null) return 0;
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
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCinemaId() { return cinemaId; }
    public void setCinemaId(String cinemaId) { this.cinemaId = cinemaId; }
    
    public SeatLayout getSeatLayout() { return seatLayout; }
    public void setSeatLayout(SeatLayout seatLayout) { 
        this.seatLayout = seatLayout;
        this.totalSeats = calculateTotalSeats();
    }
    
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    
    public Integer getRowCount() { return rowCount; }
    public void setRowCount(Integer rowCount) { this.rowCount = rowCount; }
    
    public Integer getColumnCount() { return columnCount; }
    public void setColumnCount(Integer columnCount) { this.columnCount = columnCount; }
    
    @Override
    public String toString() {
        return name;
    }
}