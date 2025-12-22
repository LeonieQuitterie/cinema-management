package com.cinema.utils;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.prefs.Preferences;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:3000/api/auth";
    private static final HttpClient httpClient = HttpClientProvider.http1();
    private static final Gson gson = new Gson();

    // Preferences để lưu token và user
    private static final Preferences prefs = Preferences.userNodeForPackage(ApiClient.class);

    // === ĐĂNG KÝ ===
    public static AuthResponse register(String fullName, String username, String email,
            String password, String confirmPassword) {
        String jsonBody = gson.toJson(new RegisterRequest(fullName, username, email, password, confirmPassword));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();

        return sendRequest(request);
    }

    // === ĐĂNG NHẬP (chỉ dùng email) ===
    public static AuthResponse login(String email, String password) {
        String jsonBody = gson.toJson(new LoginRequest(email, password));
        System.out.println("LOGIN JSON = " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();

                System.err.println(11111);
        return sendRequest(request);
    }

    // === Gửi request chung và xử lý response ===
    private static AuthResponse sendRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.err.println(11111);

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                AuthResponse authResponse = gson.fromJson(response.body(), AuthResponse.class);
                if (authResponse.success && authResponse.data != null) {
                    // Lưu token và thông tin user
                    prefs.put("auth_token", authResponse.data.token);
                    prefs.put("user_id", authResponse.data.user.id);
                    prefs.put("username", authResponse.data.user.username);
                    prefs.put("full_name", authResponse.data.user.full_name);
                    prefs.put("email", authResponse.data.user.email);
                    prefs.put("role", authResponse.data.user.role);
                }
                return authResponse;
            } else {
                AuthResponse error = new AuthResponse();
                error.success = false;
                error.message = "Lỗi server: " + response.statusCode() + " - " + response.body();
                return error;
            }
        } catch (Exception e) {
            AuthResponse error = new AuthResponse();
            error.success = false;
            error.message = "Lỗi kết nối: " + e.getMessage();
            e.printStackTrace();
            return error;
        }
    }

    // === Lấy token đã lưu ===
    public static String getSavedToken() {
        return prefs.get("auth_token", null);
    }

    // === Lấy thông tin user đã lưu ===
    public static UserInfo getSavedUser() {
        String id = prefs.get("user_id", null);
        if (id == null)
            return null;

        UserInfo user = new UserInfo();
        user.id = id;
        user.username = prefs.get("username", "");
        user.full_name = prefs.get("full_name", "");
        user.email = prefs.get("email", "");
        user.role = prefs.get("role", "");
        user.avatar_url = prefs.get("avatar_url", null);
        return user;
    }

    // === Đăng xuất ===
    public static void logout() {
        prefs.remove("auth_token");
        prefs.remove("user_id");
        prefs.remove("username");
        prefs.remove("full_name");
        prefs.remove("email");
        prefs.remove("role");
        prefs.remove("avatar_url");
    }

    // Request classes
    private static class RegisterRequest {
        String full_name;
        String username;
        String email;
        String password;
        String confirm_password;

        RegisterRequest(String full_name, String username, String email, String password, String confirm_password) {
            this.full_name = full_name;
            this.username = username;
            this.email = email;
            this.password = password;
            this.confirm_password = confirm_password;
        }
    }

    private static class LoginRequest {
        String email;
        String password;

        LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static String getCurrentUserId() {
        return Preferences
                .userNodeForPackage(ApiClient.class)
                .get("user_id", null);
    }

}