package com.cinema.utils;

import okhttp3.*;

import java.io.IOException;

import com.cinema.models.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BookingApiService {

    private static final String BASE_URL = "http://localhost:3000/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ============ AUTHENTICATION TOKEN (nếu cần) ============
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    // ============ GET CURRENT CUSTOMER ============
    public static CustomerDTO getCurrentCustomer() throws IOException {
        String token = authToken; // đã được set từ ApiClient

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + "/customers/me");

        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
            System.out.println("🔑 Gửi API /customers/me với token");
        } else {
            System.err.println("⚠️ Gọi /customers/me mà KHÔNG có token → chắc chắn 401");
        }
        Request request = builder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get customer: " + response.code());
            }

            String jsonResponse = response.body().string();
            ApiResponse<CustomerDTO> apiResponse = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponse<CustomerDTO>>() {
                    });

            if (!apiResponse.isSuccess()) {
                throw new IOException("API returned error");
            }

            return apiResponse.getData();
        }
    }

    // ============ GET MOVIE BY ID ============
    public static MovieDTO getMovie(String movieId) throws IOException {
        String url = BASE_URL + "/booking-confirm/movie/" + movieId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get movie: " + response.code());
            }

            String jsonResponse = response.body().string();
            ApiResponse<MovieDTO> apiResponse = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponse<MovieDTO>>() {
                    });

            if (!apiResponse.isSuccess()) {
                throw new IOException("API returned error");
            }

            return apiResponse.getData();
        }
    }

    // ============ GET CINEMA BY ID ============
    public static CinemaDTO getCinema(String cinemaId) throws IOException {
        String url = BASE_URL + "/booking-confirm/cinema/" + cinemaId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get cinema: " + response.code());
            }

            String jsonResponse = response.body().string();
            ApiResponse<CinemaDTO> apiResponse = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponse<CinemaDTO>>() {
                    });

            if (!apiResponse.isSuccess()) {
                throw new IOException("API returned error");
            }

            return apiResponse.getData();
        }
    }

    // ============ GET SCREEN BY ID ============
    public static ScreenDTO getScreen(String screenId) throws IOException {
        String url = BASE_URL + "/booking-confirm/screen/" + screenId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get screen: " + response.code());
            }

            String jsonResponse = response.body().string();
            ApiResponse<ScreenDTO> apiResponse = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<ApiResponse<ScreenDTO>>() {
                    });

            if (!apiResponse.isSuccess()) {
                throw new IOException("API returned error");
            }

            return apiResponse.getData();
        }
    }
}