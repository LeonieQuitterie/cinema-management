package com.cinema.controllers.cinema;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.text.NumberFormat;
import java.util.*;

import com.cinema.models.Cinema;
import com.cinema.models.Screen;
import com.cinema.models.Seat;
import com.cinema.models.SeatLayout;
import com.cinema.models.SeatStatus;
import com.cinema.models.SeatType;
import com.cinema.models.Showtime;
import com.cinema.utils.SocketIOClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SeatSelectionController {
    // === WEBSOCKET ===
    private SocketIOClient socket;
    private String mySocketId;

    // === LƯU STACKPANE THAY VÌ BUTTON ===
    private Map<String, StackPane> seatPanes = new HashMap<>();

    @FXML
    private Button backButton;
    @FXML
    private Label cinemaNameLabel;
    @FXML
    private GridPane seatGridPane;
    @FXML
    private Label selectedSeatsLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button continueButton;

    private Screen currentScreen;
    private Showtime currentShowtime;
    private List<Seat> selectedSeats = new ArrayList<>();

    private NumberFormat currencyFormat;

    @FXML
    public void initialize() {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        loadMockData();
        renderSeatGrid();
        updatePriceSummary();
    }

    private void loadMockData() {
        currentShowtime = new Showtime();
        currentShowtime.setId("ST001");
        currentShowtime.setBasePrice(75000);
        currentShowtime.getBookedSeats().addAll(Arrays.asList(
                "A3", "A4", "B5", "C6", "C7", "D8", "E4", "E5", "F6", "G7"));

        SeatLayout layout = createMockSeatLayout();
        currentScreen = new Screen();
        currentScreen.setId("SCR001");
        currentScreen.setName("Phòng 1");
        currentScreen.setSeatLayout(layout);
        cinemaNameLabel.setText("CGV Vincom Đà Nẵng - " + currentScreen.getName());
    }

    private SeatLayout createMockSeatLayout() {
        int rows = 10;
        int cols = 14;
        SeatLayout layout = new SeatLayout(rows, cols);
        String[] rowNames = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };
        double basePrice = currentShowtime.getBasePrice();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (col == 2 || col == 11) {
                    layout.setSeat(row, col, null);
                    continue;
                }

                int actualCol = col > 2 ? col - 1 : col;
                if (col > 11)
                    actualCol = col - 2;
                String leftSeatNum = rowNames[row] + String.format("%02d", actualCol + 1);

                if (row >= 4 && row <= 5 && (col == 5 || col == 7)) {
                    String rightSeatNum = rowNames[row] + String.format("%02d", actualCol + 2);
                    String coupleSeatNumber = leftSeatNum + "-" + rightSeatNum;
                    Seat coupleSeat = new Seat(
                            coupleSeatNumber,
                            SeatType.COUPLE,
                            basePrice * SeatType.COUPLE.getPriceMultiplier(),
                            row, col);
                    layout.setSeat(row, col, coupleSeat);
                    layout.setSeat(row, col + 1, coupleSeat);
                    col++;
                    continue;
                }

                if (row >= 4 && row <= 5 && (col == 6 || col == 8)) {
                    continue;
                }

                SeatType type = (row >= 7) ? SeatType.VIP : SeatType.STANDARD;
                double price = basePrice * type.getPriceMultiplier();
                Seat seat = new Seat(leftSeatNum, type, price, row, col);
                layout.setSeat(row, col, seat);
            }
        }
        return layout;
    }

    private void renderSeatGrid() {
        seatGridPane.getChildren().clear();
        seatPanes.clear(); // Xóa map cũ

        SeatLayout layout = currentScreen.getSeatLayout();
        String[] rowNames = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

        for (int row = 0; row < layout.getRows(); row++) {
            Label rowLabel = new Label(rowNames[row]);
            rowLabel.getStyleClass().add("row-label");
            rowLabel.setAlignment(Pos.CENTER);
            seatGridPane.add(rowLabel, 0, row);

            for (int col = 0; col < layout.getColumns(); col++) {
                Seat seat = layout.getSeat(row, col);

                if (seat == null || seat.getSeatNumber() == null || seat.getSeatNumber().isEmpty()) {
                    continue;
                }

                StackPane seatPane = createSeatButton(seat);

                // === LƯU STACKPANE VÀO MAP ===
                seatPanes.put(seat.getSeatNumber(), seatPane);

                seatGridPane.add(seatPane, col + 1, row);
            }
        }
    }

    private StackPane createSeatButton(Seat seat) {
        StackPane seatPane = new StackPane();
        Label seatLabel = new Label(seat.getSeatNumber());
        seatLabel.getStyleClass().add("seat-label");
        seatPane.getChildren().add(seatLabel);
        seatPane.getStyleClass().add("seat");

        if (currentShowtime.isSeatBooked(seat.getSeatNumber())) {
            seat.setStatus(SeatStatus.BOOKED);
            seatPane.getStyleClass().add("seat-booked");
        } else {
            seat.setStatus(SeatStatus.AVAILABLE);

            switch (seat.getSeatType()) {
                case VIP:
                    seatPane.getStyleClass().add("seat-vip");
                    break;
                case COUPLE:
                    seatPane.getStyleClass().add("seat-couple");
                    break;
                default:
                    seatPane.getStyleClass().add("seat-available");
            }

            seatPane.setOnMouseClicked(event -> handleSeatClick(seat, seatPane));
        }

        return seatPane;
    }

    private void handleSeatClick(Seat seat, StackPane seatPane) {
        if (seat.getStatus() == SeatStatus.BOOKED) {
            return;
        }

        String seatNumber = seat.getSeatNumber();

        if (seat.getStatus() == SeatStatus.SELECTED) {
            // === BỎ CHỌN → EMIT RELEASE ===
            seat.setStatus(SeatStatus.AVAILABLE);
            seatPane.getStyleClass().remove("seat-selected");
            selectedSeats.remove(seat);

            if (socket != null && socket.isConnected()) { // ← SỬA ĐÂY
                JsonObject data = new JsonObject();
                data.addProperty("showtimeId", currentShowtime.getId());
                data.addProperty("seatNumber", seatNumber);
                socket.emit("release-seat", data);
            }

        } else {
            // === CHỌN GHẾ → EMIT HOLD ===
            seat.setStatus(SeatStatus.SELECTED);
            if (!seatPane.getStyleClass().contains("seat-selected")) {
                seatPane.getStyleClass().add("seat-selected");
            }
            selectedSeats.add(seat);

            if (socket != null && socket.isConnected()) { // ← SỬA ĐÂY
                JsonObject data = new JsonObject();
                data.addProperty("showtimeId", currentShowtime.getId());
                data.addProperty("seatNumber", seatNumber);
                socket.emit("hold-seat", data);
            }
        }

        updatePriceSummary();
    }

    private void updatePriceSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("Chưa chọn ghế");
            totalPriceLabel.setText("0 đ");
            continueButton.setDisable(true);
        } else {
            StringBuilder seatsText = new StringBuilder();
            for (int i = 0; i < selectedSeats.size(); i++) {
                seatsText.append(selectedSeats.get(i).getSeatNumber());
                if (i < selectedSeats.size() - 1) {
                    seatsText.append(", ");
                }
            }
            selectedSeatsLabel.setText("Ghế: " + seatsText.toString());

            double totalPrice = 0;
            for (Seat seat : selectedSeats) {
                totalPrice += seat.getPrice();
            }

            totalPriceLabel.setText(currencyFormat.format(totalPrice) + " đ");
            continueButton.setDisable(false);
        }
    }

    @FXML
    private void handleBackButton() {
        handleBack();
    }

    @FXML
    private void handleContinue() {
        try {
            // === EMIT BOOK SEATS TRƯỚC KHI CHUYỂN TRANG ===
            if (socket != null && socket.isConnected()) { // ← SỬA ĐÂY
                List<String> seatNumbers = new ArrayList<>();
                for (Seat seat : selectedSeats) {
                    seatNumbers.add(seat.getSeatNumber());
                }

                JsonObject data = new JsonObject();
                data.addProperty("showtimeId", currentShowtime.getId());
                data.add("seatNumbers", new com.google.gson.Gson().toJsonTree(seatNumbers));
                socket.emit("book-seats", data);
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cinema/combo-selection.fxml"));
            Parent newRoot = loader.load();

            Stage stage = (Stage) continueButton.getScene().getWindow();
            Scene currentScene = stage.getScene();
            boolean wasFullScreen = stage.isFullScreen();

            currentScene.setRoot(newRoot);

            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Không thể mở trang chọn đồ ăn!");
            alert.showAndWait();
        }
    }

    public void setShowData(Cinema cinema, Screen screen, Showtime showtime) {
        this.currentScreen = screen;
        this.currentShowtime = showtime;
        this.selectedSeats.clear();

        String cinemaName = cinema.getName();
        String screenName = screen.getName();
        String timeRange = "";

        if (showtime != null && showtime.getStartTime() != null && showtime.getEndTime() != null) {
            String start = showtime.getStartTime().toLocalTime().toString();
            String end = showtime.getEndTime().toLocalTime().toString();
            timeRange = start + " - " + end;

            if (showtime.getFormat() != null && !showtime.getFormat().isEmpty()) {
                timeRange += " (" + showtime.getFormat() + ")";
            }
        } else {
            timeRange = "Không rõ giờ";
        }

        cinemaNameLabel.setText(cinemaName + " • " + screenName + " • " + timeRange);
        renderSeatGrid();
        updatePriceSummary();

        // === KẾT NỐI WEBSOCKET ===
        connectToSocket(showtime.getId());
    }

    private void connectToSocket(String showtimeId) {
        // === CHẠY ASYNC ĐỂ KHÔNG BLOCK UI ===
        new Thread(() -> {
            try {
                Thread.sleep(300); // Delay 300ms để chắc chắn server đã sẵn sàng

                socket = new SocketIOClient();
                socket.connect();

                // Đợi kết nối thành công (max 3 giây)
                int retries = 0;
                while (!socket.isConnected() && retries < 30) {
                    Thread.sleep(100);
                    retries++;
                }

                if (!socket.isConnected()) {
                    System.err.println("❌ Cannot connect to Socket.io after 3 seconds");
                    return;
                }

                // Join room
                JsonObject joinData = new JsonObject();
                joinData.addProperty("showtimeId", showtimeId);
                socket.emit("join-showtime", joinData);

                // Setup listeners
                Platform.runLater(() -> setupSocketListeners());

                System.out.println("✅ Connected to Socket.io for showtime: " + showtimeId);

            } catch (Exception e) {
                System.err.println("❌ Socket.io connection failed: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void setupSocketListeners() {
        // 1. Nhận danh sách ghế đang held
        socket.on("initial-held-seats", data -> {
            if (data.has("seats")) {
                JsonArray seats = data.getAsJsonArray("seats");
                for (int i = 0; i < seats.size(); i++) {
                    JsonObject seat = seats.get(i).getAsJsonObject();
                    String seatNumber = seat.get("seatNumber").getAsString();
                    updateSeatHeldByOthers(seatNumber);
                }
            }
        });

        // 2. Khi có người hold ghế
        socket.on("seat-held", data -> {
            String seatNumber = data.get("seatNumber").getAsString();
            String holderId = data.get("holderId").getAsString();

            if (mySocketId == null) {
                mySocketId = socket.getSocketId();
            }

            if (holderId.equals(mySocketId)) {
                updateSeatSelectedByMe(seatNumber);
            } else {
                updateSeatHeldByOthers(seatNumber);
            }
        });

        // 3. Khi có người release ghế
        socket.on("seat-released", data -> {
            String seatNumber = data.get("seatNumber").getAsString();
            resetSeatToAvailable(seatNumber);
        });

        // 4. Release batch
        socket.on("seats-released-batch", data -> {
            JsonArray seatNumbers = data.getAsJsonArray("seatNumbers");
            for (int i = 0; i < seatNumbers.size(); i++) {
                String seatNumber = seatNumbers.get(i).getAsString();
                resetSeatToAvailable(seatNumber);
            }
        });

        // 5. Khi có người book ghế
        socket.on("seats-booked", data -> {
            JsonArray seatNumbers = data.getAsJsonArray("seatNumbers");
            for (int i = 0; i < seatNumbers.size(); i++) {
                String seatNumber = seatNumbers.get(i).getAsString();
                updateSeatBooked(seatNumber);
            }
        });

        // 6. Hold failed
        socket.on("hold-failed", data -> {
            String seatNumber = data.get("seatNumber").getAsString();
            String reason = data.get("reason").getAsString();
            System.out.println("⚠️ Không thể chọn ghế " + seatNumber + ": " + reason);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Ghế " + seatNumber + " đang được chọn bởi người khác!");
                alert.showAndWait();
            });
        });
    }

    // === HELPER METHODS ===

    private void updateSeatSelectedByMe(String seatNumber) {
        StackPane seatPane = seatPanes.get(seatNumber);
        if (seatPane != null) {
            // Xóa các class cũ
            seatPane.getStyleClass().removeAll("seat-available", "seat-vip", "seat-couple");
            // Thêm class selected
            if (!seatPane.getStyleClass().contains("seat-selected")) {
                seatPane.getStyleClass().add("seat-selected");
            }

            // Tìm Seat object và thêm vào selectedSeats
            Seat seat = findSeatObject(seatNumber);
            if (seat != null && !selectedSeats.contains(seat)) {
                seat.setStatus(SeatStatus.SELECTED);
                selectedSeats.add(seat);
                updatePriceSummary();
            }
        }
    }

    private void updateSeatHeldByOthers(String seatNumber) {
        StackPane seatPane = seatPanes.get(seatNumber);
        if (seatPane != null) {
            // Style cho ghế người khác hold
            seatPane.setStyle("-fx-background-color: #FFC107; -fx-opacity: 0.6;");
            seatPane.setDisable(true);
        }
    }

    private void resetSeatToAvailable(String seatNumber) {
        StackPane seatPane = seatPanes.get(seatNumber);
        if (seatPane != null) {
            // Reset style
            seatPane.setStyle("");
            seatPane.setDisable(false);

            // Tìm Seat object để xác định loại ghế
            Seat seat = findSeatObject(seatNumber);
            if (seat != null) {
                seat.setStatus(SeatStatus.AVAILABLE);

                // Xóa class selected và thêm lại class theo loại ghế
                seatPane.getStyleClass().remove("seat-selected");

                switch (seat.getSeatType()) {
                    case VIP:
                        if (!seatPane.getStyleClass().contains("seat-vip")) {
                            seatPane.getStyleClass().add("seat-vip");
                        }
                        break;
                    case COUPLE:
                        if (!seatPane.getStyleClass().contains("seat-couple")) {
                            seatPane.getStyleClass().add("seat-couple");
                        }
                        break;
                    default:
                        if (!seatPane.getStyleClass().contains("seat-available")) {
                            seatPane.getStyleClass().add("seat-available");
                        }
                }

                // Xóa khỏi selectedSeats
                selectedSeats.remove(seat);
                updatePriceSummary();
            }
        }
    }

    private void updateSeatBooked(String seatNumber) {
        StackPane seatPane = seatPanes.get(seatNumber);
        if (seatPane != null) {
            // Remove old classes
            seatPane.getStyleClass().removeAll("seat-available", "seat-vip", "seat-couple", "seat-selected");
            // Add booked class
            if (!seatPane.getStyleClass().contains("seat-booked")) {
                seatPane.getStyleClass().add("seat-booked");
            }
            seatPane.setDisable(true);

            // Remove from selectedSeats
            Seat seat = findSeatObject(seatNumber);
            if (seat != null) {
                seat.setStatus(SeatStatus.BOOKED);
                selectedSeats.remove(seat);
                updatePriceSummary();
            }
        }
    }

    // Tìm Seat object từ seatNumber
    private Seat findSeatObject(String seatNumber) {
        if (currentScreen != null && currentScreen.getSeatLayout() != null) {
            SeatLayout layout = currentScreen.getSeatLayout();
            for (int row = 0; row < layout.getRows(); row++) {
                for (int col = 0; col < layout.getColumns(); col++) {
                    Seat seat = layout.getSeat(row, col);
                    if (seat != null && seat.getSeatNumber().equals(seatNumber)) {
                        return seat;
                    }
                }
            }
        }
        return null;
    }

    private String getSeatType(String seatNumber) {
        Seat seat = findSeatObject(seatNumber);
        if (seat != null) {
            return seat.getSeatType().name();
        }
        // Fallback
        if (seatNumber.startsWith("H") || seatNumber.startsWith("I") || seatNumber.startsWith("J")) {
            return "VIP";
        }
        return "STANDARD";
    }

    @FXML
    private void handleBack() {
        // Đóng socket trước khi thoát
        if (socket != null && socket.isConnected()) { // ← SỬA ĐÂY
            socket.disconnect(); // ← close() → disconnect()
        }

        // TODO: Navigate back
        System.out.println("Back button clicked");
    }
}