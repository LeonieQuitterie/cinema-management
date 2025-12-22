package com.cinema.utils;

// ===== JAVA HTTP CLIENT =====
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

// ===== GOOGLE GSON =====
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// ===== MODEL =====
import com.cinema.models.Booking;
import com.cinema.models.BookingDB;
import com.cinema.utils.adapters.LocalDateTimeAdapter;

public class BookingApiClient {

    // ===== SHARED OBJECTS =====
    private static final HttpClient httpClient = HttpClientProvider.http1();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    // ================== CREATE BOOKING ==================
    public static void createBooking(BookingDB booking) throws Exception {
        System.out.println("fwfwfwfwfwe");
        if (booking == null) {
            throw new IllegalArgumentException("Booking is null");
        }

        String jsonBody = gson.toJson(booking);
        System.err.println(jsonBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/api/bookings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new RuntimeException(
                    "Create booking failed. Status: "
                            + response.statusCode()
                            + " | Body: "
                            + response.body()
            );
        }

        System.out.println("✅ Booking created successfully");
    }
}
