package com.cinema.models;

public enum PaymentStatus {
    PENDING("Chờ thanh toán", "#FFA500"),
    PAID("Đã thanh toán", "#4CAF50"),
    EXPIRED("Hết hạn", "#F44336"),
    CANCELLED("Đã hủy", "#9E9E9E");

    private final String displayName;
    private final String color;

    PaymentStatus(String displayName, String color) {
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