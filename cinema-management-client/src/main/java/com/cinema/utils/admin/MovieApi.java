package com.cinema.utils.admin;

import com.cinema.models.Movie;
import com.cinema.models.Genre;
import com.cinema.utils.adapters.LocalDateAdapter;
import com.cinema.utils.adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MovieApi {
    private static final String BASE_URL = "http://localhost:3000/api/admin/movies";
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    /**
     * Get all movies
     */
    public CompletableFuture<List<Movie>> getAllMovies() {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url(BASE_URL)
                .header("Accept", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to fetch movies: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    List<Movie> movies = gson.fromJson(
                        jsonObject.get("data"), 
                        new TypeToken<List<Movie>>(){}.getType()
                    );
                    return movies != null ? movies : new ArrayList<>();
                }
                
                return new ArrayList<>();
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Get movie by ID
     */
    public CompletableFuture<Movie> getMovieById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Movie ID cannot be null or empty");
        }
        
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .header("Accept", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 404) {
                    throw new MovieNotFoundException("Movie not found: " + id);
                }
                
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to fetch movie: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    return gson.fromJson(jsonObject.get("data"), Movie.class);
                }
                
                throw new ServiceException("Invalid response format");
                
            } catch (MovieNotFoundException e) {
                throw e;
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Create new movie
     */
    public CompletableFuture<Movie> createMovie(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String jsonBody = gson.toJson(movie);
            
            RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
            );
            System.err.println(jsonBody);
            System.err.println(body);
            Request request = new Request.Builder()
                .url(BASE_URL)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(body)
                .build();
            System.err.println(request);
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new ServiceException("Failed to create movie: HTTP " + response.code() + " - " + errorBody);
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    return gson.fromJson(jsonObject.get("data"), Movie.class);
                }
                
                throw new ServiceException("Invalid response format");
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Update movie
     */
    public CompletableFuture<Movie> updateMovie(String id, Movie movie) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Movie ID cannot be null or empty");
        }
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        
        return CompletableFuture.supplyAsync(() -> {
            String jsonBody = gson.toJson(movie);
            
            RequestBody body = RequestBody.create(
                jsonBody,
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
                    throw new MovieNotFoundException("Movie not found: " + id);
                }
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new ServiceException("Failed to update movie: HTTP " + response.code() + " - " + errorBody);
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    return gson.fromJson(jsonObject.get("data"), Movie.class);
                }
                
                throw new ServiceException("Invalid response format");
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Delete movie
     */
    public CompletableFuture<Void> deleteMovie(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Movie ID cannot be null or empty");
        }
        
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 404) {
                    throw new MovieNotFoundException("Movie not found: " + id);
                }
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new ServiceException("Failed to delete movie: HTTP " + response.code() + " - " + errorBody);
                }
                
                return null;
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Get all genres
     */
    public CompletableFuture<List<Genre>> getAllGenres() {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url("http://localhost:3000/api/admin/genres")
                .header("Accept", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to fetch genres: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    List<Genre> genres = gson.fromJson(
                        jsonObject.get("data"), 
                        new TypeToken<List<Genre>>(){}.getType()
                    );
                    return genres != null ? genres : new ArrayList<>();
                }
                
                return new ArrayList<>();
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Get movie statistics
     */
    public CompletableFuture<MovieStats> getMovieStats() {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                .url(BASE_URL + "/stats/summary")
                .header("Accept", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ServiceException("Failed to fetch stats: HTTP " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonObject.has("data")) {
                    return gson.fromJson(jsonObject.get("data"), MovieStats.class);
                }
                
                throw new ServiceException("Invalid response format");
                
            } catch (IOException e) {
                throw new ServiceException("Network error: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Movie statistics data class
     */
    public static class MovieStats {
        private int total;
        private int nowShowing;
        private int comingSoon;
        private double avgRating;
        
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        
        public int getNowShowing() { return nowShowing; }
        public void setNowShowing(int nowShowing) { this.nowShowing = nowShowing; }
        
        public int getComingSoon() { return comingSoon; }
        public void setComingSoon(int comingSoon) { this.comingSoon = comingSoon; }
        
        public double getAvgRating() { return avgRating; }
        public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    }
    
    /**
     * Custom exception for service errors
     */
    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }
        
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Custom exception for movie not found
     */
    public static class MovieNotFoundException extends RuntimeException {
        public MovieNotFoundException(String message) {
            super(message);
        }
    }
}
