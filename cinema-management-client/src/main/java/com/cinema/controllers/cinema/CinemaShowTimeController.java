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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.cinema.models.Cinema;
import com.cinema.models.Screen;
import com.cinema.models.Seat;
import com.cinema.models.SeatLayout;
import com.cinema.models.SeatStatus;
import com.cinema.models.Showtime;
import com.cinema.utils.CinemaApiClient;

import org.kordamp.ikonli.javafx.FontIcon;

public class CinemaShowTimeController {
    @FXML
    private Label movieTitleLabel;

    @FXML
    private ComboBox<String> locationCombo;

    @FXML
    private HBox dateContainer;

    @FXML
    private HBox timeSlotContainer;

    @FXML
    private HBox cinemaChainContainer;

    @FXML
    private VBox cinemaListingsContainer;

    // === BIẾN LƯU TRỮ ===
    private LocalDate currentSelectedDate = LocalDate.now();
    private String selectedDate;
    private String selectedTimeSlot = "Tất cả";
    private String selectedCinemaChain = "Tất cả";
    private String selectedLocation = "Tất cả";
    private String currentMovieId;

    private List<Cinema> allCinemas = new ArrayList<>();

    @FXML
    public void initialize() {
        // Set selectedDate = hôm nay
        selectedDate = currentSelectedDate.format(java.time.format.DateTimeFormatter.ofPattern("d/M"));

        // === ComboBox Location - BỎ HARD-CODE ===
        // Sẽ được fill tự động sau khi load API
        if (locationCombo != null) {
            locationCombo.setValue("Tất cả");
            locationCombo.setOnAction(e -> {
                selectedLocation = locationCombo.getValue();
                filterCinemaListings();
            });
        }

        // === Tạo 14 ngày (hôm nay được chọn mặc định) ===
        generateDateButtons(14);

        // === Tạo khung giờ (Tất cả được chọn mặc định) ===
        generateTimeSlotButtons();

        System.out.println("CinemaShowTimeController initialized");
        System.out.println("Waiting for movieId...");
    }

    /**
     * Nhận movieId từ trang trước → Gọi API load rạp
     */
    public void setMovieId(String movieId) {
        this.currentMovieId = movieId;
        System.out.println("Received movieId: " + movieId);
        loadCinemasFromAPI();
    }

    /**
     * Gọi API thật để load danh sách rạp
     */
    private void loadCinemasFromAPI() {
        if (currentMovieId == null || currentMovieId.isEmpty()) {
            System.err.println("Movie ID is null, cannot load cinemas");
            return;
        }

        cinemaListingsContainer.getChildren().clear();

        // Hiển thị loading...
        Label loading = new Label("Đang tải danh sách rạp...");
        loading.setStyle("-fx-text-fill: #8e92bc; -fx-font-size: 16px;");
        cinemaListingsContainer.getChildren().add(loading);

        // Gọi API trong background thread
        new Thread(() -> {
            List<Cinema> cinemas = CinemaApiClient.getCinemasByMovieId(currentMovieId);

            Platform.runLater(() -> {
                cinemaListingsContainer.getChildren().clear();

                if (cinemas == null || cinemas.isEmpty()) {
                    Label noData = new Label("Không tìm thấy rạp chiếu phim này");
                    noData.setStyle("-fx-text-fill: #8e92bc; -fx-font-size: 16px;");
                    cinemaListingsContainer.getChildren().add(noData);
                    return;
                }

                allCinemas = cinemas;
                System.out.println("Loaded " + allCinemas.size() + " cinemas from API");

                updateLocationComboBox();
                generateCinemaChainButtons();
                filterCinemaListings();
            });
        }).start();
    }

    /**
     * Cập nhật LocationComboBox với cities từ API
     */
    private void updateLocationComboBox() {
        if (locationCombo == null || allCinemas == null || allCinemas.isEmpty())
            return;

        List<String> cities = allCinemas.stream()
                .map(Cinema::getCity)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        locationCombo.getItems().clear();
        locationCombo.getItems().add("Tất cả");
        locationCombo.getItems().addAll(cities);
        locationCombo.setValue("Tất cả");
    }

    /**
     * Tạo nút filter theo chuỗi rạp (từ dữ liệu thật)
     */
    private void generateCinemaChainButtons() {
        if (cinemaChainContainer == null)
            return;

        cinemaChainContainer.getChildren().clear();

        // Nút "Tất cả" trước tiên
        VBox allButton = createCinemaChainButton("Tất cả");
        allButton.getStyleClass().add("cinema-logo-selected");
        cinemaChainContainer.getChildren().add(allButton);

        // NULL CHECK
        if (allCinemas == null || allCinemas.isEmpty()) {
            return;
        }

        // Lấy các cụm rạp từ dữ liệu thực
        Set<String> chains = new HashSet<>();
        for (Cinema cinema : allCinemas) {
            String chain = detectChain(cinema.getName());
            if (!"OTHER".equals(chain)) {
                chains.add(chain);
                System.out.println("Detected chain: " + chain + " from cinema: " + cinema.getName());
            }
        }

        System.out.println("Total chains found: " + chains); // Hiển thị ["CGV", "LOTTE", ...]

        // Sắp xếp theo thứ tự mong muốn
        List<String> orderedChains = chains.stream()
                .sorted((a, b) -> {
                    String[] order = { "CGV", "LOTTE", "GALAXY", "BHD STAR" };
                    int ia = java.util.Arrays.asList(order).indexOf(a);
                    int ib = java.util.Arrays.asList(order).indexOf(b);
                    if (ia == -1)
                        ia = 999;
                    if (ib == -1)
                        ib = 999;
                    return Integer.compare(ia, ib);
                })
                .collect(Collectors.toList());

        // Tạo button cho từng cụm
        for (String chain : orderedChains) {
            cinemaChainContainer.getChildren().add(createCinemaChainButton(chain));
        }
    }

    /**
     * Helper: Xác định chuỗi rạp từ tên (dùng chung)
     */
    private String detectChain(String cinemaName) {
        String upper = cinemaName.toUpperCase();
        if (upper.contains("CGV"))
            return "CGV";
        if (upper.contains("LOTTE"))
            return "LOTTE";
        if (upper.contains("GALAXY"))
            return "GALAXY";
        if (upper.contains("BHD"))
            return "BHD STAR";
        return "OTHER";
    }

    private VBox createCinemaChainButton(String chainName) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10, 15, 10, 15));
        vbox.getStyleClass().add("cinema-logo-button");
        vbox.setCursor(Cursor.HAND);

        Label label = new Label(chainName);
        label.getStyleClass().add("cinema-logo-text");
        label.setFont(Font.font("System", FontWeight.BOLD, chainName.equals("BHD STAR") ? 14 : 16));
        vbox.getChildren().add(label);

        vbox.setOnMouseClicked(e -> {
            // Bỏ selected cũ
            cinemaChainContainer.getChildren().forEach(node -> node.getStyleClass().remove("cinema-logo-selected"));
            // Chọn cái mới
            vbox.getStyleClass().add("cinema-logo-selected");
            selectedCinemaChain = chainName;
            filterCinemaListings();
            addScaleAnimation(vbox);
        });

        return vbox;
    }

    /**
     * FILTER THẬT - Lọc rạp theo location, cinema chain, date, time slot
     */
    private void filterCinemaListings() {
        System.out.println("=== FILTERING ===");
        System.out.println("Date: " + selectedDate + " (" + currentSelectedDate + ")");
        System.out.println("Time Slot: " + selectedTimeSlot);
        System.out.println("Cinema Chain: " + selectedCinemaChain);
        System.out.println("Location: " + selectedLocation);

        cinemaListingsContainer.getChildren().clear();

        if (allCinemas == null || allCinemas.isEmpty()) {
            Label noData = new Label("Chưa có dữ liệu rạp");
            noData.setStyle("-fx-text-fill: #8e92bc;");
            cinemaListingsContainer.getChildren().add(noData);
            return;
        }

        List<Cinema> filtered = allCinemas.stream()
                .filter(cinema -> {
                    // 1. Filter theo location
                    if (!"Tất cả".equals(selectedLocation)) {
                        if (!selectedLocation.equals(cinema.getCity())) {
                            return false;
                        }
                    }

                    // 2. Filter theo cinema chain
                    if (!"Tất cả".equals(selectedCinemaChain)) {
                        String chain = detectChain(cinema.getName());
                        if (!selectedCinemaChain.equals(chain)) {
                            return false;
                        }
                    }

                    // 3. THÊM: Có ít nhất 1 suất phù hợp với ngày + khung giờ
                    List<Screen> suitableScreens = filterScreensByDateAndTime(cinema.getScreens());
                    return !suitableScreens.isEmpty();
                })
                .collect(Collectors.toList());

        System.out.println("Filtered: " + filtered.size() + " cinemas");

        if (filtered.isEmpty()) {
            Label noData = new Label("Không tìm thấy suất chiếu phù hợp");
            noData.setStyle("-fx-text-fill: #8e92bc; -fx-font-size: 16px;");
            cinemaListingsContainer.getChildren().add(noData);
            return;
        }

        // Hiển thị từng rạp
        for (Cinema cinema : filtered) {
            VBox card = createCinemaCard(cinema);
            if (card != null) {
                cinemaListingsContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Tạo card rạp từ Cinema thật
     */
    private VBox createCinemaCard(Cinema cinema) {
        VBox card = new VBox(12);
        card.getStyleClass().add("cinema-card");
        card.setPadding(new Insets(20));

        String chain = detectChain(cinema.getName()); // Dùng helper nhất quán
        boolean isLotte = "LOTTE".equals(chain);
        boolean isFavorite = false; // TODO: Check từ database

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo
        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER);
        // logoBox.setPadding(new Insets(10, 15, 10, 15));
        // logoBox.getStyleClass().add("cinema-logo-small");
        if (isLotte)
            logoBox.getStyleClass().add("cinema-logo-small-lotte");

        // === Logo Image ===
        ImageView logoImageView = new ImageView();

        logoImageView.setFitWidth(45);
        logoImageView.setFitHeight(60);
        logoImageView.setPreserveRatio(false); // ❗ để ảnh khít tuyệt đối
        logoImageView.setSmooth(true);

        // Load ảnh từ API
        String logoUrl = cinema.getLogoUrl();
        if (logoUrl != null && !logoUrl.isBlank()) {
            logoImageView.setImage(new Image(logoUrl, true));
        }

        // === Logo Box (khung) ===
        logoBox.setMinSize(45, 60);
        logoBox.setPrefSize(45, 60);
        logoBox.setMaxSize(45, 60);
        logoBox.setAlignment(Pos.CENTER);

        // Bo góc cho ảnh (KHÔNG viền)
        Rectangle clip = new Rectangle(45, 60);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        logoImageView.setClip(clip);

        // đảm bảo clip luôn khớp size
        logoImageView.layoutBoundsProperty().addListener((obs, o, n) -> {
            clip.setWidth(45);
            clip.setHeight(60);
        });

        // Add vào box
        logoBox.getChildren().clear();
        logoBox.getChildren().add(logoImageView);

        // Info
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(cinema.getName());
        name.getStyleClass().add("cinema-name");
        name.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label addr = new Label(cinema.getAddress());
        addr.getStyleClass().add("cinema-address");
        addr.setFont(Font.font(13));

        Label dist = new Label("📍 " + cinema.getCity());
        dist.getStyleClass().add("cinema-distance");
        dist.setFont(Font.font(12));

        info.getChildren().addAll(name, addr, dist);

        // Favorite button
        FontIcon heartOutline = new FontIcon("far-heart");
        FontIcon heartFilled = new FontIcon("fas-heart");

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

        // Showtimes - Filter theo ngày + khung giờ
        FlowPane flow = new FlowPane(10, 10);
        flow.getStyleClass().add("showtimes-container");

        List<Screen> filteredScreens = filterScreensByDateAndTime(cinema.getScreens());

        if (filteredScreens.isEmpty()) {
            // Không có suất chiếu phù hợp → không hiển thị rạp này
            return null;
        }

        // Tạo button cho MỖI SHOWTIME (không phải mỗi screen)
        for (Screen screen : filteredScreens) {
            for (Showtime showtime : screen.getShowtimes()) {
                String timeRange = showtime.getStartTime().toLocalTime().toString()
                        + " - "
                        + showtime.getEndTime().toLocalTime().toString();

                int available = countAvailableSeats(screen.getSeatLayout());
                String seatsText = available + "/" + screen.getTotalSeats() + " ghế";

                VBox btn = createShowtimeButton(timeRange, seatsText, cinema, screen, showtime);
                flow.getChildren().add(btn);
            }
        }

        card.getChildren().addAll(header, flow);
        return card;
    }

    /**
     * Filter screens theo ngày và khung giờ đã chọn
     */
    private List<Screen> filterScreensByDateAndTime(List<Screen> screens) {
        if (screens == null)
            return new ArrayList<>();

        List<Screen> result = new ArrayList<>();

        for (Screen screen : screens) {
            if (screen.getShowtimes() == null || screen.getShowtimes().isEmpty())
                continue;

            // Lọc showtimes của screen này
            List<Showtime> filteredShowtimes = screen.getShowtimes().stream()
                    .filter(st -> {
                        if (st.getStartTime() == null)
                            return false;

                        // Filter theo ngày
                        LocalDate stDate = st.getStartTime().toLocalDate();
                        if (!stDate.equals(currentSelectedDate)) {
                            return false;
                        }

                        // Filter theo khung giờ
                        if (!"Tất cả".equals(selectedTimeSlot)) {
                            String[] parts = selectedTimeSlot.split(" - ");
                            if (parts.length == 2) {
                                try {
                                    LocalTime slotStart = LocalTime.parse(parts[0]);
                                    LocalTime slotEnd = LocalTime.parse(parts[1]);
                                    LocalTime stTime = st.getStartTime().toLocalTime();

                                    if (stTime.isBefore(slotStart) || !stTime.isBefore(slotEnd)) {
                                        return false;
                                    }
                                } catch (Exception e) {
                                    // Parse error → bỏ qua filter này
                                }
                            }
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            // Nếu có showtime phù hợp → clone screen và gắn showtimes đã filter
            if (!filteredShowtimes.isEmpty()) {
                Screen clonedScreen = new Screen();
                clonedScreen.setId(screen.getId());
                clonedScreen.setName(screen.getName());
                clonedScreen.setCinemaId(screen.getCinemaId());
                clonedScreen.setSeatLayout(screen.getSeatLayout());
                clonedScreen.setTotalSeats(screen.getTotalSeats());
                clonedScreen.setShowtimes(filteredShowtimes);
                result.add(clonedScreen);
            }
        }

        return result;
    }

    private VBox createShowtimeButton(String timeRange, String seatsText,
            Cinema cinema, Screen screen, Showtime showtime) {
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

        btn.setOnMouseClicked(e -> goToSeatSelection(cinema, screen, showtime));

        return btn;
    }

    private void goToSeatSelection(Cinema cinema, Screen screen, Showtime showtime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/seat-selection.fxml"));
            Parent seatRoot = loader.load();

            SeatSelectionController controller = loader.getController();

            // Mock ghế đã đặt (40% random) - TODO: Lấy từ API booking
            List<String> bookedSeats = new ArrayList<>();
            Random rand = new Random();
            SeatLayout layout = screen.getSeatLayout();
            if (layout != null && layout.getSeats() != null) {
                for (List<Seat> row : layout.getSeats()) {
                    for (Seat s : row) {
                        if (s != null && rand.nextDouble() < 0.4) {
                            bookedSeats.add(s.getSeatNumber());
                        }
                    }
                }
            }
            showtime.setBookedSeats(bookedSeats);

            controller.setShowData(cinema, screen, showtime);

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

    /**
     * Đếm ghế available
     * TODO: Hiện tại API chưa trả status → tất cả ghế đều AVAILABLE
     */
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

        String[] timeSlots = {
                "Tất cả",
                "06:00 - 09:00",
                "09:00 - 12:00",
                "12:00 - 15:00",
                "15:00 - 18:00",
                "18:00 - 21:00",
                "21:00 - 00:00",
                "00:00 - 06:00"
        };

        for (String slot : timeSlots) {
            Button btn = new Button(slot);
            btn.getStyleClass().add("time-filter-button");

            if ("Tất cả".equals(slot)) {
                btn.getStyleClass().add("filter-button-selected");
            }

            btn.setCursor(Cursor.HAND);
            btn.setMinWidth(120);
            btn.setPrefWidth(140);

            btn.setOnAction(e -> {
                timeSlotContainer.getChildren().forEach(node -> {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("filter-button-selected");
                    }
                });

                btn.getStyleClass().add("filter-button-selected");
                selectedTimeSlot = btn.getText();

                System.out.println("Selected time slot: " + selectedTimeSlot);
                filterCinemaListings();

                addScaleAnimation(btn);
            });

            timeSlotContainer.getChildren().add(btn);
        }
    }

    private void generateDateButtons(int count) {
        dateContainer.getChildren().clear();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < count; i++) {
            LocalDate date = today.plusDays(i);
            VBox dateButton = createDateButton(date, i == 0);
            dateContainer.getChildren().add(dateButton);
        }
    }

    private VBox createDateButton(LocalDate date, boolean isSelected) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(5);
        vbox.getStyleClass().add("date-button");
        if (isSelected) {
            vbox.getStyleClass().add("date-button-selected");
            currentSelectedDate = date;
            selectedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("d/M"));
        }

        vbox.setPadding(new Insets(12, 20, 12, 20));
        vbox.setCursor(Cursor.HAND);

        Label lblDay = new Label(date.format(java.time.format.DateTimeFormatter.ofPattern("d/M")));
        lblDay.getStyleClass().add("date-number");
        lblDay.setFont(Font.font("System", FontWeight.BOLD, 18));

        String dayName;
        if (date.isEqual(LocalDate.now())) {
            dayName = "H.nay (" + getWeekdayAbbreviation(date) + ")";
        } else {
            dayName = getDayNameInVietnamese(date) + " (" + getWeekdayAbbreviation(date) + ")";
        }

        Label lblWeekday = new Label(dayName);
        lblWeekday.getStyleClass().add("date-day");
        lblWeekday.setFont(Font.font("System", 12));

        vbox.getChildren().addAll(lblDay, lblWeekday);

        vbox.setOnMouseClicked(e -> {
            dateContainer.getChildren().forEach(node -> node.getStyleClass().remove("date-button-selected"));

            vbox.getStyleClass().add("date-button-selected");
            selectedDate = lblDay.getText();
            currentSelectedDate = date;

            System.out.println("Selected date: " + selectedDate + " (" + currentSelectedDate + ")");
            filterCinemaListings();

            addScaleAnimation(vbox);
        });

        return vbox;
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/movie-detail.fxml"));
            Parent movieDetailRoot = loader.load();

            MovieDetailController controller = loader.getController();
            controller.loadMovieById(currentMovieId);

            Stage stage = (Stage) dateContainer.getScene().getWindow();
            Scene scene = stage.getScene();
            boolean wasFullScreen = stage.isFullScreen();

            scene.setRoot(movieDetailRoot);

            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addScaleAnimation(Node node) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), node);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.95);
        scaleTransition.setToY(0.95);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
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

    @FXML
    private void handleNotifications(ActionEvent event) {
        System.out.println("=== THÔNG BÁO ===");
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        System.out.println("=== TRANG CÁ NHÂN ===");
    }
}