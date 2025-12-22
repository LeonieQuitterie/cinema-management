package com.cinema.utils;

import com.cinema.models.FoodCombo;
import com.cinema.models.FoodCategory;
import com.google.gson.Gson;
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
 * Class chuyên gọi các API liên quan đến Food Combo (đồ ăn, đồ uống) trong hệ thống rạp chiếu phim
 */
public class FoodComboApiClient {

    private static final String BASE_URL = "http://localhost:3000/api";
    private static final HttpClient httpClient = HttpClientProvider.http1(); // giữ nguyên như các class khác của Q
    private static final Gson gson = new Gson();

    /**
     * Gọi API lấy danh sách combo đồ ăn/đồ uống theo rạp chiếu phim
     * Bao gồm cả combo riêng của rạp và combo chung (cinema_id IS NULL)
     *
     * @param cinemaId ID của rạp, ví dụ: "cin_001"
     * @return List<FoodCombo> hoặc null nếu có lỗi
     */
    public static List<FoodCombo> getFoodCombosByCinemaId(String cinemaId) {
        if (cinemaId == null || cinemaId.trim().isEmpty()) {
            System.err.println("cinemaId không được null hoặc rỗng");
            return null;
        }

        String url = BASE_URL + "/combos/cinema/" + cinemaId.trim();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String jsonBody = response.body();
                JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();
                boolean success = root.get("success").getAsBoolean();
                if (success && root.has("data")) {
                    JsonArray dataArray = root.getAsJsonArray("data");
                    return parseFoodCombos(dataArray.toString());
                }
            }
            System.err.println("API error getFoodCombos: " + response.statusCode() + " - " + response.body());
            return null;
        } catch (Exception e) {
            System.err.println("Lỗi kết nối API food combos: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse JSON array thành List<FoodCombo>
     */
    private static List<FoodCombo> parseFoodCombos(String jsonArrayStr) {
        JsonArray arr = JsonParser.parseString(jsonArrayStr).getAsJsonArray();
        List<FoodCombo> combos = new ArrayList<>();

        for (JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();

            FoodCombo combo = new FoodCombo();
            combo.setId(obj.get("id").getAsString());
            combo.setName(obj.get("name").getAsString());

            // description có thể null
            if (obj.has("description") && !obj.get("description").isJsonNull()) {
                combo.setDescription(obj.get("description").getAsString());
            }

            combo.setPrice(obj.get("price").getAsDouble());

            // image_url có thể null
            if (obj.has("image_url") && !obj.get("image_url").isJsonNull()) {
                combo.setImageUrl(obj.get("image_url").getAsString());
            }

            // category
            if (obj.has("category") && !obj.get("category").isJsonNull()) {
                String categoryStr = obj.get("category").getAsString();
                combo.setCategory(FoodCategory.fromString(categoryStr));
            }

            // available: hỗ trợ cả boolean và int (1/0)
            JsonElement availableEl = obj.get("available");
            boolean available = false;
            if (availableEl != null && availableEl.isJsonPrimitive()) {
                if (availableEl.getAsJsonPrimitive().isBoolean()) {
                    available = availableEl.getAsBoolean();
                } else if (availableEl.getAsJsonPrimitive().isNumber()) {
                    available = availableEl.getAsInt() == 1;
                }
            }
            combo.setAvailable(available);

            combos.add(combo);
        }

        return combos;
    }
}