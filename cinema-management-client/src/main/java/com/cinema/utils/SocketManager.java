package com.cinema.utils;

import com.google.gson.JsonObject;
import javafx.application.Platform;

/**
 * Singleton quản lý kết nối Socket.IO duy nhất cho toàn bộ flow đặt vé
 */
public class SocketManager {

    private static SocketManager instance;

    private SocketIOClient socket;
    private String currentShowtimeId;

    private SocketManager() {
        // Private constructor để ngăn tạo instance từ bên ngoài
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    /**
     * Kết nối đến server Socket.IO và join room theo showtimeId
     * Nếu đã kết nối đúng room rồi thì không làm gì
     */
    public void connect(String showtimeId) {
        // Nếu đang kết nối đúng showtimeId rồi → bỏ qua
        if (socket != null && socket.isConnected() && showtimeId.equals(currentShowtimeId)) {
            return;
        }

        // Nếu đang kết nối showtime khác → disconnect cũ trước
        if (socket != null && socket.isConnected()) {
            disconnect();
        }

        this.currentShowtimeId = showtimeId;

        // Tạo socket mới và kết nối (chạy async để không block UI)
        new Thread(() -> {
            try {
                Thread.sleep(300); // Delay nhỏ để server sẵn sàng

                socket = new SocketIOClient();
                socket.connect();

                // Đợi kết nối thành công (tối đa 3 giây)
                int retries = 0;
                while (!socket.isConnected() && retries < 30) {
                    Thread.sleep(100);
                    retries++;
                }

                if (!socket.isConnected()) {
                    System.err.println("❌ SocketManager: Không thể kết nối Socket.IO sau 3 giây");
                    return;
                }

                // Join room
                JsonObject joinData = new JsonObject();
                joinData.addProperty("showtimeId", showtimeId);
                socket.emit("join-showtime", joinData);

                Platform.runLater(() -> System.out.println("✅ SocketManager connected & joined room: " + showtimeId));

            } catch (Exception e) {
                System.err.println("❌ SocketManager connection failed: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public SocketIOClient getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public String getCurrentShowtimeId() {
        return currentShowtimeId;
    }

    /**
     * Ngắt kết nối socket - chỉ gọi khi thoát hẳn flow đặt vé
     * (ví dụ: back về trang chủ phim, hủy đặt vé, thoát app)
     */
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
            currentShowtimeId = null;
            System.out.println("🔌 SocketManager disconnected");
        }
    }
}