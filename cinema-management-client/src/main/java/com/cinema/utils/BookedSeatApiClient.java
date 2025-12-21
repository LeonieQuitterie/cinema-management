package com.cinema.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Class chuyên trách gọi API lấy danh sách ghế đã đặt (booked seats)
 * API: GET /api/showtimes/{showtimeId}/booked-seats
 * Response mẫu:
 * {
 *   "success": true,
 *   "data": ["A1", "A3", "B5", "C10", "D7"]
 * }
 */
public class BookedSeatApiClient {

    private static final String BASE_URL = "http://localhost:3000/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Lấy danh sách ghế đã đặt cho một suất chiếu cụ thể
     * @param showtimeId ID của suất chiếu (ví dụ: "st_001")
     * @return List<String> chứa các seat_number đã được đặt, hoặc danh sách rỗng nếu lỗi/API không thành công
     */
    public static List<String> getBookedSeats(String showtimeId) {
        String url = BASE_URL + "/showtimes/" + showtimeId + "/booked-seats";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();

                // Kiểm tra success và có data
                if (root.has("success") && root.get("success").getAsBoolean()
                        && root.has("data") && root.get("data").isJsonArray()) {

                    JsonArray dataArray = root.getAsJsonArray("data");
                    List<String> bookedSeats = new ArrayList<>();

                    for (JsonElement element : dataArray) {
                        bookedSeats.add(element.getAsString());
                    }

                    return bookedSeats; // Trả về danh sách ghế đã đặt thật
                }
            }

            // Nếu không thành công, in log và fallback về danh sách rỗng
            System.err.println("API booked-seats lỗi: " + response.statusCode() + " - " + response.body());

        } catch (Exception e) {
            System.err.println("Lỗi kết nối API booked-seats: " + e.getMessage());
            e.printStackTrace();
        }

        // Luôn trả về danh sách rỗng thay vì null để tránh crash UI
        return new ArrayList<>();
    }
}