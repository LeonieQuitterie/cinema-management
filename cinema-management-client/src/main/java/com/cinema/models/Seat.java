package com.cinema.models;

import com.google.gson.annotations.SerializedName;

public class Seat {
    @SerializedName("seat_number")
    private String seatNumber;    // VD: "A1", "B5", "C10"
    @SerializedName("seat_type")
    private SeatType seatType;     // STANDARD, VIP, COUPLE
    private SeatStatus status;     // AVAILABLE, SELECTED, BOOKED
    private double price;          // Giá ghế
    @SerializedName("row_index")
    private int rowIndex;          // Vị trí hàng trong layout
    @SerializedName("col_index")
    private int colIndex;          // Vị trí cột trong layout

    public Seat() {}

    public Seat(String seatNumber, SeatType seatType, double price, int rowIndex, int colIndex) {
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.price = price;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.status = SeatStatus.AVAILABLE; // Mặc định là available
    }

    // Getters and Setters
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    
    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
    
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    
    public int getColIndex() { return colIndex; }
    public void setColIndex(int colIndex) { this.colIndex = colIndex; }
}