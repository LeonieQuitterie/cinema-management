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
import javafx.scene.layout.Priority;

import java.text.NumberFormat;
import java.util.*;

import com.cinema.models.Cinema;
import com.cinema.models.Screen;
import com.cinema.models.Seat;
import com.cinema.models.SeatLayout;
import com.cinema.models.SeatStatus;
import com.cinema.models.SeatType;
import com.cinema.models.Showtime;

public class SeatSelectionController {

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

    // Mock data
    private Screen currentScreen;
    private Showtime currentShowtime;
    private List<Seat> selectedSeats = new ArrayList<>();
    private NumberFormat currencyFormat;

    @FXML
    public void initialize() {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Load mock data
        loadMockData();

        // Render seat grid
        renderSeatGrid();

        // Update UI
        updatePriceSummary();
    }

    private void loadMockData() {
        // Tạo mock showtime
        currentShowtime = new Showtime();
        currentShowtime.setId("ST001");
        currentShowtime.setBasePrice(75000); // 75k cho ghế thường

        // Ghế đã được đặt (mock)
        currentShowtime.getBookedSeats().addAll(Arrays.asList(
                "A3", "A4", "B5", "C6", "C7", "D8", "E4", "E5", "F6", "G7"));

        // Tạo mock screen với seat layout
        SeatLayout layout = createMockSeatLayout();

        currentScreen = new Screen();
        currentScreen.setId("SCR001");
        currentScreen.setName("Phòng 1");
        currentScreen.setSeatLayout(layout);

        // Set cinema name
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

                // === LỐI ĐI GIỮA (cột 2 và 11) ===
                if (col == 2 || col == 11) {
                    layout.setSeat(row, col, null);
                    continue;
                }

                // === TÍNH SỐ GHẾ THẬT (bỏ qua 2 cột lối đi) ===
                int actualCol = col > 2 ? col - 1 : col;
                if (col > 11)
                    actualCol = col - 2;

                String leftSeatNum = rowNames[row] + String.format("%02d", actualCol + 1);

                // === GHẾ ĐÔI (hàng E, F – cột 5-6 và 7-8) ===
                if (row >= 4 && row <= 5 && (col == 5 || col == 7)) {
                    String rightSeatNum = rowNames[row] + String.format("%02d", actualCol + 2);
                    String coupleSeatNumber = leftSeatNum + "-" + rightSeatNum;

                    Seat coupleSeat = new Seat(
                            coupleSeatNumber,
                            SeatType.COUPLE,
                            basePrice * SeatType.COUPLE.getPriceMultiplier(),
                            row, col);

                    // Cả 2 ô cùng trỏ về 1 đối tượng Seat → click ô nào cũng chọn cả đôi
                    layout.setSeat(row, col, coupleSeat); // ô trái
                    layout.setSeat(row, col + 1, coupleSeat); // ô phải

                    col++; // bỏ qua cột kế tiếp
                    continue;
                }

                // === BỎ QUA Ô PHẢI CỦA GHẾ ĐÔI (vì đã gán ở trên) ===
                if (row >= 4 && row <= 5 && (col == 6 || col == 8)) {
                    continue;
                }

                // === GHẾ VIP (3 hàng cuối: H, I, J) ===
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
        SeatLayout layout = currentScreen.getSeatLayout();

        String[] rowNames = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

        for (int row = 0; row < layout.getRows(); row++) {
            // Thêm row label (A, B, C...)
            Label rowLabel = new Label(rowNames[row]);
            rowLabel.getStyleClass().add("row-label");
            rowLabel.setAlignment(Pos.CENTER);
            seatGridPane.add(rowLabel, 0, row);

            for (int col = 0; col < layout.getColumns(); col++) {
                Seat seat = layout.getSeat(row, col);

                if (seat == null || seat.getSeatNumber() == null || seat.getSeatNumber().isEmpty()) {
                    // Lối đi - để trống
                    continue;
                }

                // Tạo seat button
                StackPane seatPane = createSeatButton(seat);

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

        // Xác định trạng thái ghế
        if (currentShowtime.isSeatBooked(seat.getSeatNumber())) {
            seat.setStatus(SeatStatus.BOOKED);
            seatPane.getStyleClass().add("seat-booked");
        } else {
            seat.setStatus(SeatStatus.AVAILABLE);

            // Thêm style theo loại ghế
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

            // Thêm click handler
            seatPane.setOnMouseClicked(event -> handleSeatClick(seat, seatPane));
        }

        return seatPane;
    }

    private void handleSeatClick(Seat seat, StackPane seatPane) {
        if (seat.getStatus() == SeatStatus.BOOKED) {
            return; // Không cho phép chọn ghế đã đặt
        }

        if (seat.getStatus() == SeatStatus.SELECTED) {
            // Bỏ chọn
            seat.setStatus(SeatStatus.AVAILABLE);
            seatPane.getStyleClass().remove("seat-selected");
            selectedSeats.remove(seat);
        } else {
            // Chọn ghế
            seat.setStatus(SeatStatus.SELECTED);
            if (!seatPane.getStyleClass().contains("seat-selected")) {
                seatPane.getStyleClass().add("seat-selected");
            }
            selectedSeats.add(seat);
        }

        updatePriceSummary();
    }

    private void updatePriceSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("Chưa chọn ghế");
            totalPriceLabel.setText("0 đ");
            continueButton.setDisable(true);
        } else {
            // Tạo text hiển thị ghế đã chọn
            StringBuilder seatsText = new StringBuilder();
            for (int i = 0; i < selectedSeats.size(); i++) {
                seatsText.append(selectedSeats.get(i).getSeatNumber());
                if (i < selectedSeats.size() - 1) {
                    seatsText.append(", ");
                }
            }
            selectedSeatsLabel.setText("Ghế: " + seatsText.toString());

            // Tính tổng tiền
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
        System.out.println("Back button clicked");
        // TODO: Navigate back to showtime selection
    }

    @FXML
    private void handleContinue() {
        try {
            // Load trang chọn combo
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cinema/combo-selection.fxml"));
            Parent newRoot = loader.load();

            // Lấy Stage hiện tại và chuyển trang mượt (không nháy fullscreen)
            Stage stage = (Stage) continueButton.getScene().getWindow(); // hoặc tên button của bạn
            Scene currentScene = stage.getScene();
            boolean wasFullScreen = stage.isFullScreen();

            // Chỉ thay root → chuyển trang siêu mượt
            currentScene.setRoot(newRoot);

            // Giữ nguyên fullscreen nếu đang bật
            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint(""); // ẩn dòng "Press ESC to exit full screen"
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

        // === HIỂN THỊ TIÊU ĐỀ ĐÚNG VỚI DỮ LIỆU THẬT ===
        String cinemaName = cinema.getName();
        String screenName = screen.getName(); // tên phòng thuần, không có giờ nữa
        String timeRange = "";

        if (showtime != null && showtime.getStartTime() != null && showtime.getEndTime() != null) {
            String start = showtime.getStartTime().toLocalTime().toString(); // ví dụ: 19:00
            String end = showtime.getEndTime().toLocalTime().toString(); // ví dụ: 21:15
            timeRange = start + " - " + end;

            // Nếu có định dạng (2D/3D)
            if (showtime.getFormat() != null && !showtime.getFormat().isEmpty()) {
                timeRange += " (" + showtime.getFormat() + ")";
            }
        } else {
            timeRange = "Không rõ giờ";
        }

        cinemaNameLabel.setText(cinemaName + " • " + screenName + " • " + timeRange);

        // Render lại sơ đồ ghế với dữ liệu mới
        renderSeatGrid();
        updatePriceSummary();
    }

    // Helper để trích giờ từ tên phòng (vì Q đang lưu giờ trong screen.getName())
    private String extractTimeFromName(String name) {
        int index = name.indexOf("•");
        return index > 0 ? name.substring(index + 2).trim() : "Không rõ giờ";
    }
}