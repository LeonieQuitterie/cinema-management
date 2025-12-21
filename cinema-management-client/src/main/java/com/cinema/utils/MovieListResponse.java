// Đường dẫn: src/main/java/com/cinema/utils/MovieListResponse.java
package com.cinema.utils;

import com.cinema.models.Movie;
import java.util.List;

public class MovieListResponse {
    public boolean success;
    public String message;
    public List<Movie> data;  // ← dùng chính Movie từ models

    public MovieListResponse() {} // Gson cần constructor rỗng
}