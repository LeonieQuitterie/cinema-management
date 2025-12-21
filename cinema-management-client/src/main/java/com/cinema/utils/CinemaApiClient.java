package com.cinema.utils;

import com.cinema.models.Cinema;
import com.cinema.models.Screen;
import com.cinema.models.Seat;
import com.cinema.models.SeatLayout;
import com.cinema.models.SeatStatus;
import com.cinema.models.SeatType;
import com.cinema.models.Showtime; // THÊM IMPORT
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class CinemaApiClient {

    private static final String BASE_URL = "http://localhost:3000/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    /**
     * Gọi API lấy danh sách rạp đang chiếu phim theo movieId
     * @param movieId ID phim (ví dụ: "mov_004")
     * @return List<Cinema> hoặc null nếu lỗi
     */
    public static List<Cinema> getCinemasByMovieId(String movieId) {
        String url = BASE_URL + "/cinemas/movie/" + movieId;
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
                    return parseCinemas(dataArray.toString());
                }
            }
            System.err.println("API error: " + response.statusCode() + " - " + response.body());
            return null;
        } catch (Exception e) {
            System.err.println("Lỗi kết nối API cinemas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse JSON string thành List<Cinema> theo cấu trúc API thực tế
     */
    private static List<Cinema> parseCinemas(String json) {
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        List<Cinema> cinemas = new ArrayList<>();

        for (JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();

            Cinema cinema = new Cinema();
            cinema.setId(obj.get("id").getAsString());
            cinema.setName(obj.get("name").getAsString());
            cinema.setAddress(obj.get("address").getAsString());
            cinema.setCity(obj.get("city").getAsString());
            if (!obj.get("logoUrl").isJsonNull()) {
                cinema.setLogoUrl(obj.get("logoUrl").getAsString());
            }

            // Parse screens
            JsonArray screensArray = obj.getAsJsonArray("screens");
            List<Screen> screens = new ArrayList<>();
            for (JsonElement sEl : screensArray) {
                JsonObject sObj = sEl.getAsJsonObject();

                Screen screen = new Screen();
                screen.setId(sObj.get("id").getAsString());
                screen.setName(sObj.get("name").getAsString());
                screen.setCinemaId(sObj.get("cinemaId").getAsString());
                if (sObj.has("totalSeats")) {
                    screen.setTotalSeats(sObj.get("totalSeats").getAsInt());
                }

                // Parse seatLayout
                JsonObject layoutObj = sObj.getAsJsonObject("seatLayout");
                SeatLayout seatLayout = new SeatLayout();
                seatLayout.setRows(layoutObj.get("rows").getAsInt());
                seatLayout.setColumns(layoutObj.get("columns").getAsInt());

                JsonArray seatsArray = layoutObj.getAsJsonArray("seats"); // ma trận 2D
                List<List<Seat>> seatMatrix = new ArrayList<>();

                for (JsonElement rowEl : seatsArray) {
                    JsonArray rowArray = rowEl.getAsJsonArray();
                    List<Seat> row = new ArrayList<>();
                    for (JsonElement seatEl : rowArray) {
                        if (seatEl.isJsonNull()) {
                            row.add(null); // ghế trống (lối đi)
                            continue;
                        }
                        JsonObject seatObj = seatEl.getAsJsonObject();

                        Seat seat = new Seat();
                        seat.setSeatNumber(seatObj.get("seatNumber").getAsString());
                        seat.setSeatType(SeatType.valueOf(seatObj.get("seatType").getAsString()));
                        seat.setPrice(seatObj.get("price").getAsDouble());
                        seat.setRowIndex(seatObj.get("rowIndex").getAsInt());
                        seat.setColIndex(seatObj.get("colIndex").getAsInt());
                        seat.setStatus(SeatStatus.AVAILABLE); // API này không trả status, mặc định available

                        row.add(seat);
                    }
                    seatMatrix.add(row);
                }

                seatLayout.setSeats(seatMatrix);
                screen.setSeatLayout(seatLayout);

                // ===== THÊM MỚI: Parse showtimes =====
                if (sObj.has("showtimes")) {
                    JsonArray showtimesArray = sObj.getAsJsonArray("showtimes");
                    List<Showtime> showtimes = new ArrayList<>();
                    
                    for (JsonElement stEl : showtimesArray) {
                        JsonObject stObj = stEl.getAsJsonObject();
                        
                        Showtime st = new Showtime();
                        st.setId(stObj.get("id").getAsString());
                        
                        // Parse startTime và endTime (dạng "2025-12-21T14:00:00")
                        if (stObj.has("startTime") && !stObj.get("startTime").isJsonNull()) {
                            st.setStartTime(stObj.get("startTime").getAsString());
                        }
                        if (stObj.has("endTime") && !stObj.get("endTime").isJsonNull()) {
                            st.setEndTime(stObj.get("endTime").getAsString());
                        }
                        
                        st.setBasePrice(stObj.get("basePrice").getAsDouble());
                        
                        if (stObj.has("format") && !stObj.get("format").isJsonNull()) {
                            st.setFormat(stObj.get("format").getAsString());
                        }
                        
                        showtimes.add(st);
                    }
                    
                    screen.setShowtimes(showtimes);
                }
                // ===== KẾT THÚC PARSE SHOWTIMES =====

                screens.add(screen);
            }

            cinema.setScreens(screens);
            cinemas.add(cinema);
        }

        return cinemas;
    }
}