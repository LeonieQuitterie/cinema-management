package com.cinema.utils.admin;

import com.cinema.models.Cinema;
import com.cinema.utils.adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CinemaApi {
    
    private static final String BASE_URL = "http://localhost:3000/api/cinemas";
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public CinemaApi() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    }
    
    /**
     * Get all cinemas
     */
    public CompletableFuture<List<Cinema>> getAllCinemas() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("🔍 CinemaApi: Fetching cinemas from " + BASE_URL);
            
            Request request = new Request.Builder()
                .url(BASE_URL)
                .header("Accept", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                System.out.println("📡 CinemaApi: Response code = " + response.code());
                
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to fetch cinemas: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                System.out.println("📦 CinemaApi: Response received");
                
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    List<Cinema> cinemas = gson.fromJson(
                        jsonObject.get("data"),
                        new TypeToken<List<Cinema>>(){}.getType()
                    );
                    System.out.println("✅ CinemaApi: Loaded " + (cinemas != null ? cinemas.size() : 0) + " cinemas");
                    return cinemas != null ? cinemas : new ArrayList<>();
                }
                
                return new ArrayList<>();
                
            } catch (IOException e) {
                System.err.println("❌ CinemaApi: Network error - " + e.getMessage());
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Get cinema by ID
     */
    public CompletableFuture<Cinema> getCinemaById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .header("Accept", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 404) {
                    throw new CinemaNotFoundException("Cinema not found: " + id);
                }
                
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to fetch cinema: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    return gson.fromJson(jsonObject.get("data"), Cinema.class);
                }
                
                throw new ServiceException("Invalid response format");
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Create new cinema
     */
    public CompletableFuture<Cinema> createCinema(Cinema cinema) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("name", cinema.getName());
            payload.addProperty("address", cinema.getAddress());
            payload.addProperty("city", cinema.getCity());
            if (cinema.getLogoUrl() != null) {
                payload.addProperty("logoUrl", cinema.getLogoUrl());
            }
            
            RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                .url(BASE_URL)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body().string();
                    System.err.println("❌ Create cinema failed: " + errorBody);
                    throw new ServiceException("Failed to create cinema: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    return gson.fromJson(jsonObject.get("data"), Cinema.class);
                }
                
                throw new ServiceException("Invalid response format");
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Update cinema
     */
    public CompletableFuture<Cinema> updateCinema(String id, Cinema cinema) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("name", cinema.getName());
            payload.addProperty("address", cinema.getAddress());
            payload.addProperty("city", cinema.getCity());
            if (cinema.getLogoUrl() != null) {
                payload.addProperty("logoUrl", cinema.getLogoUrl());
            }
            
            RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .put(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 404) {
                    throw new CinemaNotFoundException("Cinema not found: " + id);
                }
                
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to update cinema: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    return gson.fromJson(jsonObject.get("data"), Cinema.class);
                }
                
                throw new ServiceException("Invalid response format");
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Delete cinema
     */
    public CompletableFuture<Void> deleteCinema(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 404) {
                    throw new CinemaNotFoundException("Cinema not found: " + id);
                }
                
                if (response.code() == 409) {
                    String errorBody = response.body().string();
                    if (errorBody.contains("HAS_SCREENS")) {
                        throw new ServiceException("Không thể xóa rạp có phòng chiếu");
                    }
                    throw new ServiceException("Không thể xóa rạp có dữ liệu liên quan");
                }
                
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to delete cinema: HTTP " + response.code());
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
    
    public static class CinemaNotFoundException extends RuntimeException {
        public CinemaNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Update seat layout for all screens in a cinema
     */
    public CompletableFuture<Integer> updateBulkSeatLayout(String cinemaId, SeatLayoutData layoutData) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("rowCount", layoutData.getRowCount());
            payload.addProperty("columnCount", layoutData.getColumnCount());
            
            // Convert seats to JSON array
            JsonArray seatsArray = new JsonArray();
            for (SeatData seat : layoutData.getSeats()) {
                if (seat != null) {
                    JsonObject seatObj = new JsonObject();
                    seatObj.addProperty("seatNumber", seat.getSeatNumber());
                    seatObj.addProperty("seatType", seat.getSeatType());
                    seatObj.addProperty("price", seat.getPrice());
                    seatsArray.add(seatObj);
                } else {
                    seatsArray.add(JsonNull.INSTANCE);
                }
            }
            payload.add("seats", seatsArray);
            
            RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                .url(BASE_URL + "/" + cinemaId + "/screens/bulk-seat-layout")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body().string();
                    System.err.println("❌ Update seat layout failed: " + errorBody);
                    throw new ServiceException("Failed to update seat layout: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    return data.get("screensUpdated").getAsInt();
                }
                
                return 0;
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }

    // Helper classes for seat layout data
    public static class SeatLayoutData {
        private int rowCount;
        private int columnCount;
        private List<SeatData> seats;
        
        public SeatLayoutData(int rowCount, int columnCount, List<SeatData> seats) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.seats = seats;
        }
        
        public int getRowCount() { return rowCount; }
        public int getColumnCount() { return columnCount; }
        public List<SeatData> getSeats() { return seats; }
    }

    public static class SeatData {
        private String seatNumber;
        private String seatType;
        private double price;
        
        public SeatData(String seatNumber, String seatType, double price) {
            this.seatNumber = seatNumber;
            this.seatType = seatType;
            this.price = price;
        }
        
        public String getSeatNumber() { return seatNumber; }
        public String getSeatType() { return seatType; }
        public double getPrice() { return price; }
    }
}