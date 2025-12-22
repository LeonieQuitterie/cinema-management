package com.cinema.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SeatLayout {
    @SerializedName("row_count")
    private int rows;        // Số hàng
    @SerializedName("column_count")
    private int columns;     // Số cột
    private List<List<Seat>> seats; // Ma trận ghế: seats[row][col]
    
    public SeatLayout() {
        this.seats = new ArrayList<>();
    }
    
    public SeatLayout(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.seats = new ArrayList<>();
        initializeSeats();
    }
    
    // Khởi tạo ma trận ghế với null (lối đi)
    private void initializeSeats() {
        for (int i = 0; i < rows; i++) {
            List<Seat> row = new ArrayList<>();
            for (int j = 0; j < columns; j++) {
                row.add(null); // null = lối đi
            }
            seats.add(row);
        }
    }
    
    // Thêm ghế vào vị trí cụ thể
    public void setSeat(int row, int col, Seat seat) {
        if (row >= 0 && row < rows && col >= 0 && col < columns) {
            seats.get(row).set(col, seat);
        }
    }
    
    // Lấy ghế tại vị trí
    public Seat getSeat(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < columns) {
            return seats.get(row).get(col);
        }
        return null;
    }
    
    // Kiểm tra xem vị trí có phải là lối đi không
    public boolean isAisle(int row, int col) {
        Seat seat = getSeat(row, col);
        return seat == null || seat.getSeatNumber() == null || seat.getSeatNumber().isEmpty();
    }

    // Getters and Setters
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
    
    public int getColumns() { return columns; }
    public void setColumns(int columns) { this.columns = columns; }
    
    public List<List<Seat>> getSeats() { return seats; }
    public void setSeats(List<List<Seat>> seats) { this.seats = seats; }

    public SeatLayout clone() {
        SeatLayout copy = new SeatLayout(rows, columns);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Seat seat = getSeat(r, c);
                if (seat != null) {
                    copy.setSeat(r, c, new Seat(
                        seat.getSeatNumber(),
                        seat.getSeatType(),
                        seat.getPrice(),
                        seat.getRowIndex(),
                        seat.getColIndex()
                    ));
                }
            }
        }
        return copy;
    }
}