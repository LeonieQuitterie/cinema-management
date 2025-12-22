package com.cinema.utils.admin;

import com.cinema.models.Showtime;
import com.cinema.utils.adapters.LocalDateAdapter;
import com.cinema.utils.adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ShowtimeApi {
    
    private static final String BASE_URL = "http://localhost:3000/api/admin/showtimes";
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public ShowtimeApi() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    }
    
    /**
     * Get all showtimes with optional filters
     */
    public CompletableFuture<List<Showtime>> getAllShowtimes(String date, String cinemaId, String screenId, String movieId) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL).newBuilder();
            
            if (date != null && !date.isEmpty()) {
                urlBuilder.addQueryParameter("date", date);
            }
            if (cinemaId != null && !cinemaId.isEmpty()) {
                urlBuilder.addQueryParameter("cinemaId", cinemaId);
            }
            if (screenId != null && !screenId.isEmpty()) {
                urlBuilder.addQueryParameter("screenId", screenId);
            }
            if (movieId != null && !movieId.isEmpty()) {
                urlBuilder.addQueryParameter("movieId", movieId);
            }
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Accept", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to fetch showtimes: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    List<Showtime> showtimes = gson.fromJson(
                        jsonObject.get("data"),
                        new TypeToken<List<Showtime>>(){}.getType()
                    );
                    return showtimes != null ? showtimes : new ArrayList<>();
                }
                
                return new ArrayList<>();
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Create bulk showtimes
     */
    public CompletableFuture<Integer> createBulkShowtimes(List<Showtime> showtimes) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject payload = new JsonObject();
            payload.add("showtimes", gson.toJsonTree(showtimes));
            
            RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                .url(BASE_URL + "/bulk")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to create showtimes: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    return data.get("count").getAsInt();
                }
                
                return 0;
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Delete showtime
     */
    public CompletableFuture<Void> deleteShowtime(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 404) {
                    throw new ShowtimeNotFoundException("Showtime not found: " + id);
                }
                
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to delete showtime: HTTP " + response.code());
                }
                
                return null;
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Custom exceptions
     */
    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class ShowtimeNotFoundException extends RuntimeException {
        public ShowtimeNotFoundException(String message) {
            super(message);
        }
    }
}