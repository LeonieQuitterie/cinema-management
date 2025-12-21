package com.cinema.utils;

public class RatingStatsResponse {
    public boolean success;
    public String message;
    public RatingStatsData data;

    public static class RatingStatsData {
        public double average_rating;
        public int[] percentages;     // mảng 5 phần tử: index 0 = 1 sao, index 4 = 5 sao
        public String[] counts;       // mảng 5 phần tử, dạng String như backend trả về
        public int total_ratings;
    }
}