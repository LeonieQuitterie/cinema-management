package com.cinema.utils;

import java.net.http.HttpClient;
import java.time.Duration;

public final class HttpClientProvider {

    private HttpClientProvider() {} // ❌ không cho new

    // Client chuẩn (mặc định – có thể thử HTTP/2)
    private static final HttpClient DEFAULT_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    // Client ép HTTP/1.1 (dùng cho Node / Express)
    private static final HttpClient HTTP1_CLIENT =
            HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    /** Dùng khi gọi API Node / Express */
    public static HttpClient http1() {
        return HTTP1_CLIENT;
    }

    /** Dùng khi gọi API khác (future-proof) */
    public static HttpClient defaultClient() {
        return DEFAULT_CLIENT;
    }
}
