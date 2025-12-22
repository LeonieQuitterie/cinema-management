package com.cinema.models;

/**
 * Enum đại diện cho các loại đồ ăn/đồ uống trong rạp chiếu phim
 */
public enum FoodCategory {
    COMBO("Combo"),
    POPCORN("Bắp rang"),
    DRINK("Nước uống"),
    SNACK("Đồ ăn vặt");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Trả về tên hiển thị tiếng Việt (dùng cho UI)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển từ chuỗi category trong API (ví dụ: "SNACK") thành enum tương ứng
     * @param value Chuỗi từ API, ví dụ: "COMBO", "POPCORN", ...
     * @return FoodCategory tương ứng, hoặc null nếu không hợp lệ
     */
    public static FoodCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return FoodCategory.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            System.err.println("Không tìm thấy FoodCategory tương ứng với: " + value);
            return null;
        }
    }
}