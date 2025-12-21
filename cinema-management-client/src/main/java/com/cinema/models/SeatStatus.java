package com.cinema.models;

public enum SeatStatus {
    AVAILABLE("Ghế trống", "#16213e"),        // Màu xanh đậm
    SELECTED("Bạn đã chọn", "#ff3366"),       // Màu đỏ
    BOOKED("Đã được đặt", "#666680");         // Màu xám

    private final String displayName;
    private final String color;

    SeatStatus(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}