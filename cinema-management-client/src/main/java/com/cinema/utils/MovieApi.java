package com.cinema.utils;

import com.cinema.models.Actor;
import com.cinema.models.Comment;
import com.cinema.models.CommentReaction;
import com.cinema.models.Movie;
import com.cinema.utils.MovieListResponse;
import com.cinema.utils.adapters.LocalDateAdapter;
import com.cinema.utils.adapters.LocalDateTimeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieApi {

    private static final String BASE_URL = "http://localhost:3000/api/movies";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Gson sử dụng các adapter riêng biệt + xử lý Double từ String
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Double.class, new DoubleStringAdapter())
            .registerTypeAdapter(double.class, new DoubleStringAdapter()) // cho primitive double
            .create();

    public static List<Movie> getNowShowingMovies() {
        return getMoviesByStatus("NOW_SHOWING");
    }

    public static List<Movie> getComingSoonMovies() {
        return getMoviesByStatus("COMING_SOON");
    }

    private static List<Movie> getMoviesByStatus(String status) {
        try {
            String url = BASE_URL + "?status=" + status;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                MovieListResponse resp = gson.fromJson(response.body(), MovieListResponse.class);
                if (resp.success && resp.data != null) {
                    return resp.data;
                }
            }

            System.err.println("API phim trả về lỗi hoặc không có data (status: " + status + "): " + response.body());
            return Collections.emptyList();

        } catch (Exception e) {
            System.err.println("Lỗi kết nối API phim " + status + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Adapter xử lý trường hợp average_rating trả về dưới dạng String ("8.90")
    private static class DoubleStringAdapter extends TypeAdapter<Number> {
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            String str = in.nextString();
            if (str == null || str.isEmpty()) {
                return 0.0;
            }
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    /**
     * Lấy thống kê đánh giá sao của một bộ phim cụ thể
     * 
     * @param movieId ID của phim (ví dụ: "mov_001")
     * @return RatingStatsResponse hoặc null nếu lỗi
     */
    public static RatingStatsResponse getRatingStats(String movieId) {
        if (movieId == null || movieId.trim().isEmpty()) {
            System.err.println("movieId không hợp lệ khi gọi rating-stats");
            return null;
        }

        try {
            String url = BASE_URL + "/" + movieId.trim() + "/rating-stats";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                RatingStatsResponse resp = gson.fromJson(response.body(), RatingStatsResponse.class);
                if (resp != null && resp.success && resp.data != null) {
                    return resp;
                }
            }

            // In log để debug nếu cần
            System.err.println("API rating-stats lỗi hoặc không có data cho phim " + movieId +
                    " (status code: " + response.statusCode() + "): " + response.body());
            return null;

        } catch (Exception e) {
            System.err.println("Lỗi kết nối API rating-stats cho phim " + movieId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy danh sách diễn viên (dùng model Actor có sẵn)
     * 
     * @param movieId ID phim (ví dụ: "mov_004")
     * @return List<Actor> hoặc danh sách rỗng nếu lỗi
     */
    public static List<Actor> getMovieCast(String movieId) {
        if (movieId == null || movieId.trim().isEmpty()) {
            System.err.println("movieId không hợp lệ khi gọi API cast");
            return Collections.emptyList();
        }

        try {
            String url = BASE_URL + "/" + movieId.trim() + "/cast";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                CastListResponse resp = gson.fromJson(response.body(), CastListResponse.class);
                if (resp != null && resp.success && resp.data != null) {
                    return resp.data;
                }
            }

            System.err.println("API cast lỗi hoặc không có data cho phim " + movieId +
                    " (code: " + response.statusCode() + "): " + response.body());
            return Collections.emptyList();

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API cast cho phim " + movieId + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<Comment> getMovieComments(String movieId) {
        try {
            String url = BASE_URL + "/" + movieId + "/comments";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseComments(response.body());
            }

            System.err.println("Lỗi API comments: " + response.body());
            return Collections.emptyList();

        } catch (Exception e) {
            System.err.println("Lỗi gọi API comments: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<Comment> parseComments(String json) {
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        List<Comment> comments = new ArrayList<>();

        for (JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();

            Comment c = new Comment(
                    obj.get("userId").getAsString(),
                    obj.get("rating").getAsInt(),
                    obj.get("content").getAsString());

            c.setId(obj.get("id").getAsString());
            c.setUserName(obj.get("userName").getAsString());

            // avatar có thể null
            if (!obj.get("userAvatar").isJsonNull()) {
                c.setUserAvatar(obj.get("userAvatar").getAsString());
            }

            c.setHasSpoiler(obj.get("hasSpoiler").getAsBoolean());

            // ===== FIX CREATED_AT (ISO 8601 có Z) =====
            c.setCreatedAt(
                    OffsetDateTime
                            .parse(obj.get("createdAt").getAsString())
                            .toLocalDateTime());

            // ===== reactionsList =====
            JsonArray reactions = obj.getAsJsonArray("reactionsList");
            for (JsonElement rEl : reactions) {
                JsonObject rObj = rEl.getAsJsonObject();

                CommentReaction r = new CommentReaction(
                        rObj.get("commentId").getAsString(),
                        rObj.get("userId").getAsString(),
                        rObj.get("reactionType").getAsString());

                r.setId(rObj.get("id").getAsString());

                r.setCreatedAt(
                        OffsetDateTime
                                .parse(rObj.get("createdAt").getAsString())
                                .toLocalDateTime());

                c.getReactionsList().add(r);
            }

            comments.add(c);
        }

        return comments;
    }

}