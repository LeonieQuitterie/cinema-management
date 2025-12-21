package com.cinema.controllers.cinema;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.FlowPane;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.cinema.models.Cinema;
import com.cinema.models.Screen;
import com.cinema.models.Seat;
import com.cinema.models.SeatLayout;
import com.cinema.models.SeatStatus;
import com.cinema.models.SeatType;
import com.cinema.models.Showtime;

import org.kordamp.ikonli.javafx.FontIcon;

public class CinemaShowTimeController {
    @FXML
    private Label movieTitleLabel;

    @FXML
    private ComboBox<String> locationCombo;

    private String selectedDate = "8/12";
    private String selectedTimeSlot = "Tất cả";
    private String selectedCinemaChain = "CGV";
    private String selectedLocation = "Đà Nẵng";

    private String currentMovieId; // Thêm dòng này (lưu ID phim đang chọn vé)

    @FXML
    private VBox cinemaListingsContainer; // ĐÃ CÓ fx:id trong FXML

    @FXML
    public void initialize() {
        // === PHẦN CŨ: Khởi tạo ComboBox location ===
        if (locationCombo != null) {
            locationCombo.getItems().addAll("Đà Nẵng", "Hà Nội", "TP. Hồ Chí Minh", "Cần Thơ", "Hải Phòng");
            locationCombo.setValue("Đà Nẵng");

            locationCombo.setOnAction(e -> {
                selectedLocation = locationCombo.getValue();
                filterCinemaListings();
            });
        }

        // === PHẦN MỚI: Tạo tự động 14 ngày ===
        generateDateButtons(14);

        // === THÊM DÒNG NÀY ===
        generateTimeSlotButtons();

        System.out.println("CinemaShowTimeController initialized");
        loadMockCinemas(); // THÊM DÒNG NÀY – sẽ sinh ra tất cả rạp
    }

    // Gọi trong initialize()
    private void loadMockCinemas() {
        cinemaListingsContainer.getChildren().clear();

        List<Cinema> cinemas = createMockCinemasWithRealModel();

        for (Cinema cinema : cinemas) {
            if ("Đà Nẵng".equals(cinema.getCity())) {
                cinemaListingsContainer.getChildren().add(createCinemaCard(cinema));
            }
        }
    }

    private List<Cinema> createMockCinemasWithRealModel() {
        List<Cinema> list = new ArrayList<>();

        // === CGV Vincom Đà Nẵng ===
        Cinema cgvVincom = new Cinema("cgv001", "CGV Vincom Đà Nẵng",
                "910A Ngô Quyền, An Hải Bắc, Sơn Trà, Đà Nẵng", "Đà Nẵng");

        cgvVincom.addScreen(createScreen("scr001", "Phòng 1", cgvVincom.getId(),
                "15:30 - 17:45", 142, 142));
        cgvVincom.addScreen(createScreen("scr002", "Phòng 1", cgvVincom.getId(),
                "18:00 - 20:15", 98, 142));
        cgvVincom.addScreen(createScreen("scr003", "Phòng 2", cgvVincom.getId(),
                "20:30 - 22:45", 65, 142));
        cgvVincom.addScreen(createScreen("scr004", "Phòng 3", cgvVincom.getId(),
                "22:50 - 01:05", 120, 142));
        list.add(cgvVincom);

        // === CGV Lotte Mart Đà Nẵng ===
        Cinema cgvLotte = new Cinema("cgv002", "CGV Lotte Mart Đà Nẵng",
                "Tầng 3, Lotte Mart, 06 Nại Nam, Hòa Cường Bắc, Hải Châu, Đà Nẵng", "Đà Nẵng");

        cgvLotte.addScreen(createScreen("scr005", "Phòng 4", cgvLotte.getId(),
                "16:00 - 18:15", 112, 156));
        cgvLotte.addScreen(createScreen("scr006", "Phòng 5", cgvLotte.getId(),
                "18:30 - 20:45", 76, 156));
        cgvLotte.addScreen(createScreen("scr007", "Phòng 5", cgvLotte.getId(),
                "21:00 - 23:15", 145, 156));
        list.add(cgvLotte);

        // === Lotte Cinema Đà Nẵng ===
        Cinema lotte = new Cinema("lotte001", "Lotte Cinema Đà Nẵng",
                "Tầng 5, 6 TTTM Lotte Mart, Nại Nam, Hòa Cường Bắc, Hải Châu", "Đà Nẵng");

        lotte.addScreen(createScreen("scr008", "Phòng 1", lotte.getId(),
                "14:30 - 16:45", 88, 160));
        lotte.addScreen(createScreen("scr009", "Phòng 2", lotte.getId(),
                "17:00 - 19:15", 45, 160));
        lotte.addScreen(createScreen("scr010", "Phòng 3", lotte.getId(),
                "19:30 - 21:45", 132, 160));
        lotte.addScreen(createScreen("scr011", "Phòng 4", lotte.getId(),
                "22:00 - 00:15", 99, 160));
        list.add(lotte);

        return list;
    }

    // Helper: tạo Screen đầy đủ với SeatLayout (giả lập sơ đồ ghế)
    private Screen createScreen(String id, String name, String cinemaId,
            String timeRange, int availableSeats, int totalSeats) {
        SeatLayout layout = new SeatLayout();
        layout.setRows(10);
        layout.setColumns(16);
        layout.setSeats(generateMockSeats(10, 16, availableSeats));

        Screen screen = new Screen();
        screen.setId(id);
        screen.setName(name);
        screen.setCinemaId(cinemaId);
        screen.setSeatLayout(layout);
        // Thêm thông tin suất chiếu vào tên tạm (vì model chưa có field time)
        screen.setName(name + " • " + timeRange);
        return screen;
    }

    // Tạo ma trận ghế giả
    private List<List<Seat>> generateMockSeats(int rows, int columns, int availableCount) {
        List<List<Seat>> seats = new ArrayList<>();
        int count = 0;
        char rowChar = 'A';

        for (int r = 0; r < rows; r++) {
            List<Seat> row = new ArrayList<>();
            for (int c = 0; c < columns; c++) {
                String seatNum = rowChar + String.format("%02d", c + 1);
                SeatType type = (r < 2) ? SeatType.VIP : (c == 7 || c == 8) ? SeatType.COUPLE : SeatType.STANDARD;
                double price = type == SeatType.VIP ? 120000 : type == SeatType.COUPLE ? 200000 : 85000;

                Seat seat = new Seat(seatNum, type, price, r, c);
                if (count >= availableCount) {
                    seat.setStatus(SeatStatus.BOOKED);
                }
                row.add(seat);
                count++;
            }
            seats.add(row);
            rowChar++;
        }
        return seats;
    }

    // Tạo card rạp từ Cinema thật
    private VBox createCinemaCard(Cinema cinema) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cinema-card");
        card.setPadding(new Insets(20));

        String chain = cinema.getName().contains("CGV") ? "CGV"
                : cinema.getName().contains("Lotte") ? "LOTTE" : "OTHER";
        boolean isLotte = "LOTTE".equals(chain);
        boolean isFavorite = cinema.getId().equals("lotte001"); // giả lập yêu thích

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo
        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(10, 15, 10, 15));
        logoBox.getStyleClass().add("cinema-logo-small");
        if (isLotte)
            logoBox.getStyleClass().add("cinema-logo-small-lotte");

        Label logoLabel = new Label(chain);
        logoLabel.getStyleClass().add("cinema-logo-text-small");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, isLotte ? 12 : 14));
        logoBox.getChildren().add(logoLabel);

        // Info
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(cinema.getName());
        name.getStyleClass().add("cinema-name");
        name.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label addr = new Label(cinema.getAddress());
        addr.getStyleClass().add("cinema-address");
        addr.setFont(Font.font(13));

        Label dist = new Label("Pin  " + (cinema.getName().contains("Vincom") ? "2.5 km"
                : cinema.getName().contains("Lotte Mart") ? "4.8 km" : "5.2 km"));
        dist.getStyleClass().add("cinema-distance");
        dist.setFont(Font.font(12));

        info.getChildren().addAll(name, addr, dist);

        // Favorite button
        // Favorite button – Dùng Ikonli FontIcon (đẹp hơn 1000 lần!)
        FontIcon heartOutline = new FontIcon("far-heart"); // Heart rỗng
        FontIcon heartFilled = new FontIcon("fas-heart"); // Heart đầy

        heartOutline.setIconSize(20);
        heartFilled.setIconSize(20);
        heartOutline.setIconColor(javafx.scene.paint.Color.web("#ff3366"));
        heartFilled.setIconColor(javafx.scene.paint.Color.web("#ff3366"));

        Button favBtn = new Button();
        favBtn.setGraphic(isFavorite ? heartFilled : heartOutline);
        favBtn.getStyleClass().add("favorite-button");
        if (isFavorite) {
            favBtn.getStyleClass().add("favorite-button-active");
        }

        // Hover effect + click đẹp hơn
        favBtn.setOnMouseEntered(e -> {
            if (!isFavorite) {
                heartOutline.setIconColor(javafx.scene.paint.Color.web("#ff6699"));
            }
        });
        favBtn.setOnMouseExited(e -> {
            if (!isFavorite) {
                heartOutline.setIconColor(javafx.scene.paint.Color.web("#ff3366"));
            }
        });

        favBtn.setOnAction(e -> {
            boolean nowFavorite = !favBtn.getStyleClass().contains("favorite-button-active");

            if (nowFavorite) {
                favBtn.setGraphic(heartFilled);
                favBtn.getStyleClass().add("favorite-button-active");
                System.out.println("Đã thêm " + cinema.getName() + " vào yêu thích");
            } else {
                favBtn.setGraphic(heartOutline);
                favBtn.getStyleClass().remove("favorite-button-active");
                System.out.println("Đã bỏ " + cinema.getName() + " khỏi yêu thích");
            }

            addScaleAnimation(favBtn);
        });

        header.getChildren().addAll(logoBox, info, favBtn);

        // Showtimes
        FlowPane flow = new FlowPane(10, 10);
        flow.getStyleClass().add("showtimes-container");

        for (Screen screen : cinema.getScreens()) {
            String timeRange = extractTimeFromName(screen.getName()); // tạm lấy từ tên
            int available = countAvailableSeats(screen.getSeatLayout());
            String seatsText = available + "/" + screen.getTotalSeats() + " ghế";

            VBox btn = createShowtimeButton(timeRange, seatsText, cinema, screen);
            flow.getChildren().add(btn);
        }

        card.getChildren().addAll(header, flow);
        return card;
    }

   

    private VBox createShowtimeButton(String timeRange, String seatsText, Cinema cinema, Screen screen) {
        VBox btn = new VBox(5);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.getStyleClass().add("showtime-button");
        btn.setCursor(Cursor.HAND);

        Label time = new Label(timeRange);
        time.getStyleClass().add("showtime-time");
        time.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label seats = new Label(seatsText);
        seats.getStyleClass().add("showtime-seats");
        seats.setFont(Font.font(11));

        btn.getChildren().addAll(time, seats);

        // Click → chuyển trang chọn ghế
        btn.setOnMouseClicked(e -> goToSeatSelection(cinema, screen));

        return btn;
    }

    private void goToSeatSelection(Cinema cinema, Screen screen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/seat-selection.fxml"));
            Parent seatRoot = loader.load();

            SeatSelectionController controller = loader.getController();

            // Tạo Showtime mới cho suất chiếu này
            Showtime showtime = new Showtime(); // Dùng model thật của Q!
            showtime.setId("ST_" + System.currentTimeMillis());
            showtime.setBasePrice(85000);

            // Mock ghế đã đặt (40% ghế random)
            List<String> bookedSeats = new ArrayList<>();
            Random rand = new Random();
            SeatLayout layout = screen.getSeatLayout();
            if (layout != null && layout.getSeats() != null) {
                for (List<Seat> row : layout.getSeats()) {
                    for (Seat s : row) {
                        if (s != null && rand.nextDouble() < 0.4f) { // Fix lỗi ở đây
                            bookedSeats.add(s.getSeatNumber());
                        }
                    }
                }
            }
            showtime.getBookedSeats().addAll(bookedSeats);

            // Dùng method mới để truyền dữ liệu
            controller.setShowData(cinema, screen, showtime);

            // Chuyển trang mượt mà
            Stage stage = (Stage) cinemaListingsContainer.getScene().getWindow();
            Scene currentScene = stage.getScene();
            boolean wasFullScreen = stage.isFullScreen();

            currentScene.setRoot(seatRoot);

            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Helper: trích xuất giờ từ tên phòng (vì model chưa có field time)
    private String extractTimeFromName(String name) {
        int index = name.indexOf("•");
        return index > 0 ? name.substring(index + 2).trim() : "Không rõ giờ";
    }

    // Đếm ghế trống
    private int countAvailableSeats(SeatLayout layout) {
        if (layout == null || layout.getSeats() == null)
            return 0;
        int count = 0;
        for (List<Seat> row : layout.getSeats()) {
            for (Seat s : row) {
                if (s != null && s.getStatus() == SeatStatus.AVAILABLE)
                    count++;
            }
        }
        return count;
    }

    private void generateTimeSlotButtons() {
        timeSlotContainer.getChildren().clear();

        // Danh sách khung giờ phổ biến tại rạp Việt Nam
        String[] timeSlots = {
                "Tất cả",
                "06:00 - 09:00", // Sáng sớm (ít nhưng có)
                "09:00 - 12:00", // Sáng
                "12:00 - 15:00", // Trưa
                "15:00 - 18:00", // Chiều
                "18:00 - 21:00", // Tối
                "21:00 - 00:00", // Khuya
                "00:00 - 06:00" // Suất siêu khuya (có rạp chiếu)
        };

        for (int i = 0; i < timeSlots.length; i++) {
            String slot = timeSlots[i];
            Button btn = new Button(slot);
            btn.getStyleClass().add("time-filter-button");

            // "Tất cả" mặc định được chọn
            if ("Tất cả".equals(slot)) {
                btn.getStyleClass().add("filter-button-selected");
                selectedTimeSlot = slot; // giữ giá trị mặc định
            }

            btn.setCursor(javafx.scene.Cursor.HAND);
            btn.setMinWidth(120);
            btn.setPrefWidth(140);

            // Sự kiện click
            btn.setOnAction(e -> {
                // Bỏ selected cũ
                timeSlotContainer.getChildren().forEach(node -> {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("filter-button-selected");
                    }
                });

                // Chọn cái mới
                btn.getStyleClass().add("filter-button-selected");
                selectedTimeSlot = btn.getText();

                System.out.println("Selected time slot: " + selectedTimeSlot);
                filterCinemaListings();

                // Animation
                addScaleAnimation(btn);
            });

            timeSlotContainer.getChildren().add(btn);
        }
    }

    @FXML
    private HBox dateContainer; // ĐÃ CÓ TRONG FXML → thêm fx:id này rồi

    private LocalDate currentSelectedDate; // Thêm dòng này (dùng nội bộ để quản lý ngày thật)

    @FXML
    private HBox timeSlotContainer; // Thêm dòng này

    @FXML
    private void handleBackButton() {
        try {
            // 1. Load lại trang chi tiết phim
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/movie-detail.fxml"));
            Parent movieDetailRoot = loader.load();

            // 2. Lấy controller chi tiết phim và truyền lại ID phim đang xem
            MovieDetailController controller = loader.getController();
            controller.loadMovieById(currentMovieId); // Dùng ID đã nhận từ trước
            // Nếu bạn có method setMovieData(Movie movie) thì gọi luôn cũng được

            // 3. Lấy Stage + Scene hiện tại
            Stage stage = (Stage) dateContainer.getScene().getWindow(); // hoặc bất kỳ node nào
            Scene scene = stage.getScene();

            // 4. Giữ trạng thái fullscreen (rất quan trọng!)
            boolean wasFullScreen = stage.isFullScreen();

            // 5. Thay root mượt mà – KHÔNG tạo Scene mới → không nháy!
            scene.setRoot(movieDetailRoot);

            // 6. Khôi phục fullscreen nếu đang bật
            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint(""); // ẩn hint khó chịu
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTimeSlotSelection(javafx.event.ActionEvent event) {
        // Get the clicked time button
        Button timeButton = (Button) event.getSource();

        // Remove selection from all time buttons
        HBox parent = (HBox) timeButton.getParent();
        parent.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("filter-button-selected");
            }
        });

        // Add selection to clicked button
        if (!timeButton.getStyleClass().contains("filter-button-selected")) {
            timeButton.getStyleClass().add("filter-button-selected");
        }

        selectedTimeSlot = timeButton.getText();
        System.out.println("Selected time slot: " + selectedTimeSlot);
        filterCinemaListings();

        // Add animation
        addScaleAnimation(timeButton);
    }

    private void generateDateButtons(int count) {
        dateContainer.getChildren().clear();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < count; i++) {
            LocalDate date = today.plusDays(i);
            VBox dateButton = createDateButton(date, i == 0); // ngày đầu tiên là hôm nay → selected
            dateContainer.getChildren().add(dateButton);
        }
    }

    private VBox createDateButton(LocalDate date, boolean isSelected) {
        VBox vbox = new VBox();
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setSpacing(5);
        vbox.getStyleClass().add("date-button");
        if (isSelected) {
            vbox.getStyleClass().add("date-button-selected");
            currentSelectedDate = date;
            selectedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("d/M")); // 8/12
        }

        vbox.setPadding(new javafx.geometry.Insets(12, 20, 12, 20));
        vbox.setCursor(javafx.scene.Cursor.HAND);

        // Ngày tháng: 8/12
        Label lblDay = new Label(date.format(java.time.format.DateTimeFormatter.ofPattern("d/M")));
        lblDay.getStyleClass().add("date-number");
        lblDay.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 18));

        // Thứ trong tuần
        String dayName;
        if (date.isEqual(LocalDate.now())) {
            dayName = "H.nay (" + getWeekdayAbbreviation(date) + ")";
        } else {
            dayName = getDayNameInVietnamese(date) + " (" + getWeekdayAbbreviation(date) + ")";
        }

        Label lblWeekday = new Label(dayName);
        lblWeekday.getStyleClass().add("date-day");
        lblWeekday.setFont(javafx.scene.text.Font.font("System", 12));

        vbox.getChildren().addAll(lblDay, lblWeekday);

        // === XỬ LÝ KHI CLICK ===
        vbox.setOnMouseClicked(e -> {
            // Bỏ selected cũ
            dateContainer.getChildren().forEach(node -> node.getStyleClass().remove("date-button-selected"));

            // Chọn cái mới
            vbox.getStyleClass().add("date-button-selected");

            // Cập nhật biến selectedDate kiểu String để các hàm cũ vẫn dùng được
            selectedDate = lblDay.getText(); // "8/12", "9/12"...
            currentSelectedDate = date;

            System.out.println("Selected date: " + selectedDate);
            filterCinemaListings();

            // Animation
            addScaleAnimation(vbox);
        });

        return vbox;
    }

    @FXML
    private void handleCinemaChainSelection(javafx.event.ActionEvent event) {
        // Get the clicked cinema logo button
        VBox cinemaButton = (VBox) event.getSource();

        // Remove selection from all cinema buttons
        HBox parent = (HBox) cinemaButton.getParent();
        parent.getChildren().forEach(node -> {
            if (node instanceof VBox && node.getStyleClass().contains("cinema-logo-button")) {
                node.getStyleClass().remove("cinema-logo-selected");
            }
        });

        // Add selection to clicked button
        if (!cinemaButton.getStyleClass().contains("cinema-logo-selected")) {
            cinemaButton.getStyleClass().add("cinema-logo-selected");
        }

        // Get selected cinema chain
        Label cinemaLabel = (Label) cinemaButton.getChildren().get(0);
        selectedCinemaChain = cinemaLabel.getText();

        System.out.println("Selected cinema chain: " + selectedCinemaChain);
        filterCinemaListings();

        // Add animation
        addScaleAnimation(cinemaButton);
    }

    @FXML
    private void handleFavoriteButton(javafx.event.ActionEvent event) {
        Button favoriteButton = (Button) event.getSource();

        // Toggle favorite status
        if (favoriteButton.getStyleClass().contains("favorite-button-active")) {
            favoriteButton.getStyleClass().remove("favorite-button-active");
            favoriteButton.setText("♡");
            System.out.println("Removed from favorites");
        } else {
            favoriteButton.getStyleClass().add("favorite-button-active");
            favoriteButton.setText("♥");
            System.out.println("Added to favorites");
        }

        // Add animation
        addScaleAnimation(favoriteButton);
    }

    @FXML
    private void handleShowtimeSelection(javafx.event.ActionEvent event) {
        VBox showtimeButton = (VBox) event.getSource();

        // Get showtime details
        Label timeLabel = (Label) showtimeButton.getChildren().get(0);
        Label seatsLabel = (Label) showtimeButton.getChildren().get(1);

        String showtime = timeLabel.getText();
        String availableSeats = seatsLabel.getText();

        System.out.println("Selected showtime: " + showtime);
        System.out.println("Available seats: " + availableSeats);

        // TODO: Navigate to seat selection page
        // Pass showtime and cinema information to next page

        // Add animation
        addScaleAnimation(showtimeButton);
    }

    private void filterCinemaListings() {
        System.out.println("Filtering cinema listings with:");
        System.out.println("  Date: " + selectedDate);
        System.out.println("  Time Slot: " + selectedTimeSlot);
        System.out.println("  Cinema Chain: " + selectedCinemaChain);
        System.out.println("  Location: " + selectedLocation);

        // TODO: Implement actual filtering logic
        // Filter cinema cards based on selected criteria
        // Update visible cinema cards and showtimes
    }

    private void addScaleAnimation(javafx.scene.Node node) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), node);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.95);
        scaleTransition.setToY(0.95);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
    }

    // Utility method to generate date strings
    public static String getFormattedDate(LocalDate date) {
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("vi", "VN"));

        return day + "/" + month;
    }

    // Utility method to get day name in Vietnamese
    public static String getVietnameseDayName(LocalDate date, boolean isToday) {
        if (isToday) {
            return "H.nay (" + getWeekdayAbbreviation(date) + ")";
        } else {
            String dayName = getDayNameInVietnamese(date);
            return dayName + " (" + getWeekdayAbbreviation(date) + ")";
        }
    }

    private static String getDayNameInVietnamese(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
                return "T.hai";
            case TUESDAY:
                return "T.ba";
            case WEDNESDAY:
                return "T.tư";
            case THURSDAY:
                return "T.năm";
            case FRIDAY:
                return "T.sáu";
            case SATURDAY:
                return "T.bảy";
            case SUNDAY:
                return "CN";
            default:
                return "";
        }
    }

    private static String getWeekdayAbbreviation(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
                return "T2";
            case TUESDAY:
                return "T3";
            case WEDNESDAY:
                return "T4";
            case THURSDAY:
                return "T5";
            case FRIDAY:
                return "T6";
            case SATURDAY:
                return "T7";
            case SUNDAY:
                return "CN";
            default:
                return "";
        }
    }

    // Navbar Notifications Handler
    @FXML
    private void handleNotifications(ActionEvent event) {
        System.out.println("=== THÔNG BÁO ===");
        System.out.println("• Bạn có 3 thông báo mới");
        System.out.println("• Phim Dune: Part Three sẽ ra mắt vào 15/01/2025");
        // TODO: Show notifications dialog/popup
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        System.out.println("=== TRANG CÁ NHÂN ===");
        System.out.println("Chuyển đến trang cá nhân người dùng");
        // TODO: Navigate to profile page
    }

    public void setMovieId(String movieId) {
        this.currentMovieId = movieId;
        System.out.println("CinemaShowTimeController nhận phim ID: " + movieId);
        // TODO: Sau này dùng để load lịch chiếu theo phim
    }
}