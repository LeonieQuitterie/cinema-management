package com.cinema.models;

public enum SeatType {
    STANDARD("Ghế Thường", 1.0),
    VIP("Ghế VIP", 1.5),
    COUPLE("Ghế Đôi", 2.0);

    private final String displayName;
    private final double priceMultiplier; // Hệ số nhân giá

    SeatType(String displayName, double priceMultiplier) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}