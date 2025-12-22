package com.cinema.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.application.Platform;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SocketIOClient {
    private Socket socket;
    private final Gson gson = new Gson();
    private final Map<String, Consumer<JsonObject>> eventHandlers = new HashMap<>();
    private static final String SOCKET_URL = "http://localhost:3000/seats"; // HTTP không phải WS

    private Runnable onConnectCallback;

    public void onConnect(Runnable callback) {
        this.onConnectCallback = callback;
    }

    public SocketIOClient() throws URISyntaxException {
        IO.Options options = new IO.Options();
        options.reconnection = true;
        options.reconnectionDelay = 1000;
        options.timeout = 5000;

        socket = IO.socket(SOCKET_URL, options);

        socket.on(Socket.EVENT_CONNECT, args -> {
            System.out.println("✅ Socket.io connected - ID: " + socket.id());
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            System.out.println("❌ Socket.io disconnected");
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            System.err.println("❌ Connection error: " + args[0]);
            if (args.length > 1) {
                System.err.println("Additional info: " + args[1]);
            }
        });

        socket.on(Socket.EVENT_CONNECT, args -> {
            System.out.println("✅ Socket.io connected - ID: " + socket.id());

            // ✅ Notify listeners khi connect xong
            if (onConnectCallback != null) {
                Platform.runLater(() -> onConnectCallback.run());
            }
        });

    }

    public void connect() {
        socket.connect();
    }

    public void disconnect() {
        socket.disconnect();
    }

    public boolean isConnected() {
        return socket.connected();
    }

    public String getSocketId() {
        return socket.id();
    }

    // Emit event với JsonObject
    public void emit(String event, JsonObject data) {
        try {
            JSONObject jsonObject = new JSONObject(gson.toJson(data));
            socket.emit(event, jsonObject);
        } catch (Exception e) {
            System.err.println("Error emitting event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Register event handler
    public void on(String event, Consumer<JsonObject> handler) {
        eventHandlers.put(event, handler);

        socket.on(event, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0 && args[0] instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) args[0];
                    JsonObject data = gson.fromJson(jsonObject.toString(), JsonObject.class);

                    // Chạy trên JavaFX thread
                    Platform.runLater(() -> handler.accept(data));
                }
            }
        });
    }

    // Remove event handler
    public void off(String event) {
        socket.off(event);
        eventHandlers.remove(event);
    }
}