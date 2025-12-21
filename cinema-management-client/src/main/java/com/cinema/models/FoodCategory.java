package com.cinema.models;

public enum FoodCategory {
    COMBO("Combo"),
    POPCORN("Bắp rang"),
    DRINK("Nước uống"),
    SNACK("Đồ ăn vặt");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}