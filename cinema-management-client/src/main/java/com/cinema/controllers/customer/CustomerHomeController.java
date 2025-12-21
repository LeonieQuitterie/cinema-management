package com.cinema.controllers.customer;

import com.cinema.controllers.cinema.MovieDetailController;
import com.cinema.models.Movie;
import com.cinema.models.Movie.MovieStatus;
import com.cinema.utils.MovieApi;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.Region;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class CustomerHomeController {

    @FXML
    private TextField searchField;

    @FXML
    private HBox bannerContainer;

    @FXML
    private HBox nowShowingContainer;

    @FXML
    private HBox comingSoonContainer;

    private List<Movie> nowShowingMovies = new ArrayList<>();
    private List<Movie> comingSoonMovies = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("Customer Home initialized");

        loadMoviesFromApi();

        // Load UI
        loadBanners();
        loadNowShowingMovies();
        loadComingSoonMovies();
    }

    private void loadMoviesFromApi() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // Gọi API trong background thread
                nowShowingMovies = MovieApi.getNowShowingMovies();
                comingSoonMovies = MovieApi.getComingSoonMovies();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // Cập nhật UI trên JavaFX thread
                Platform.runLater(() -> {
                    loadBanners();
                    loadNowShowingMovies();
                    loadComingSoonMovies();
                });
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    System.err.println("Lỗi tải danh sách phim: " + getException().getMessage());
                    // Có thể hiển thị thông báo lỗi cho user
                });
            }
        };

        new Thread(task).start();
    }

    // Banner section - dùng phim đang chiếu nổi bật
    private void loadBanners() {
        bannerContainer.getChildren().clear();

        List<Movie> featuredMovies = new ArrayList<>(nowShowingMovies);
        featuredMovies.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating())); // sắp xếp theo
                                                                                                       // rating cao
                                                                                                       // nhất

        for (int i = 0; i < Math.min(featuredMovies.size(), 3); i++) {
            VBox bannerCard = createBannerCard(featuredMovies.get(i), i);
            bannerContainer.getChildren().add(bannerCard);
        }
    }

    // Banner card
    private VBox createBannerCard(Movie movie, int index) {
        VBox card = new VBox();
        card.getStyleClass().add("banner-card");

        // Container chính của banner
        StackPane bannerContent = new StackPane();
        bannerContent.setPrefSize(800, 400);
        bannerContent.setMaxSize(800, 400);

        // 1. Ảnh nền bằng CSS (cách đơn giản & chắc chắn nhất)
        String imageUrl = movie.getPosterUrl();
        bannerContent.setStyle(
                "-fx-background-image: url('" + imageUrl + "'); " +
                        "-fx-background-position: center center; " +
                        "-fx-background-repeat: no-repeat; " +
                        "-fx-background-size: cover; " + // ảnh phủ đầy, đẹp như banner thật
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15;");

        // 2. Lớp tối mờ phía trên ảnh để chữ trắng nổi bật (rất quan trọng!)
        Region overlay = new Region();
        overlay.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.8));");
        overlay.setMouseTransparent(true);

        // 3. Nội dung chữ + nút ở giữa
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.setMaxWidth(700);

        // Tiêu đề
        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        title.setMaxWidth(700);
        title.setAlignment(Pos.CENTER);
        title.setWrapText(true);

        // Thể loại
        Label genre = new Label(String.join(", ", movie.getGenres()));
        genre.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 18px;");

        // Trạng thái (Đang chiếu / Sắp chiếu)
        Label statusBadge = new Label(movie.getStatus().getDisplayName());
        statusBadge.getStyleClass().add("status-badge");

        // Điểm đánh giá
        HBox ratingBox = new HBox(8);
        ratingBox.setAlignment(Pos.CENTER);
        FontIcon starIcon = new FontIcon("fas-star");
        starIcon.setIconSize(24);
        starIcon.setIconColor(javafx.scene.paint.Color.web("#ffaa33"));

        Label rating = new Label(movie.getRatingFormatted());
        rating.setStyle("-fx-text-fill: #ffaa33; -fx-font-size: 20px; -fx-font-weight: bold;");

        ratingBox.getChildren().addAll(starIcon, rating);

        // Nút đặt vé
        Button bookButton = new Button("ĐẶT VÉ NGAY");
        bookButton.getStyleClass().add("book-now-button");
        bookButton.setPrefSize(220, 55);
        bookButton.setOnAction(e -> handleBookTicket(movie));

        // Thêm tất cả vào content
        content.getChildren().addAll(title, genre, statusBadge, ratingBox, bookButton);

        // Xếp lớp: ảnh nền → overlay tối → nội dung chữ
        bannerContent.getChildren().addAll(overlay, content);

        // Click vào banner (trừ nút đặt vé) thì mở chi tiết phim
        bannerContent.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Button)) {
                handleMovieClick(movie);
            }
        });

        card.getChildren().add(bannerContent);
        return card;
    }

    // Phim đang chiếu
    // Phim đang chiếu
    private void loadNowShowingMovies() {
        nowShowingContainer.getChildren().clear();

        for (Movie movie : nowShowingMovies) {
            VBox movieCard = createMovieCard(movie);
            nowShowingContainer.getChildren().add(movieCard);
        }
    }

    private void loadComingSoonMovies() {
        comingSoonContainer.getChildren().clear();

        for (Movie movie : comingSoonMovies) {
            VBox movieCard = createComingSoonCard(movie);
            comingSoonContainer.getChildren().add(movieCard);
        }
    }

    // Phim đang chiếu
    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(12);
        card.getStyleClass().add("movie-card");

        // Poster container
        StackPane poster = new StackPane();
        poster.setPrefSize(220, 320);

        // >>> LẤY LINK ẢNH VỀ, SET LÀM NỀN POSTER <<<
        String imageUrl = movie.getPosterUrl();

        poster.setStyle(
                "-fx-background-image: url('" + imageUrl + "');" +
                        "-fx-background-size: cover;" + // phủ đầy
                        "-fx-background-position: center;" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background`-radius: 12;" + // bo góc cho ảnh
                        "-fx-border-radius: 12;" // để không bị vuông khi hover
        );

        // Overlay mờ để chữ/icon nổi bật (tuỳ chọn)
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.25);");
        overlay.setMouseTransparent(true);

        // Placeholder khi ảnh load lỗi
        VBox posterContent = new VBox(10);
        posterContent.setAlignment(Pos.CENTER);
        posterContent.setPadding(new Insets(20));

        Label posterLabel = new Label(movie.getTitle());
        posterLabel.setStyle(
                "-fx-text-fill: #dddddd;" +
                        "-fx-font-size: 13px;" +
                        "-fx-wrap-text: true;" +
                        "-fx-text-alignment: center;");
        posterLabel.setMaxWidth(180);
        posterLabel.setAlignment(Pos.CENTER);

        posterContent.getChildren().addAll(posterLabel);

        // Nếu ảnh load thành công → ẩn placeholder
        Image testImage = new Image(imageUrl, false);
        testImage.exceptionProperty().addListener((obs, old, err) -> {
            if (err != null) {
                System.out.println("Ảnh poster lỗi: " + err.getMessage());
            } else {
                poster.getChildren().remove(posterContent);
            }
        });

        // Ảnh → overlay → content
        poster.getChildren().addAll(overlay, posterContent);

        // Info section giữ nguyên
        VBox infoBox = new VBox(8);

        Label title = new Label(movie.getTitle());
        title.getStyleClass().add("movie-title");
        title.setWrapText(true);
        title.setMaxWidth(220);

        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon clockIcon = new FontIcon("fas-clock");
        clockIcon.setIconSize(12);
        clockIcon.setIconColor(javafx.scene.paint.Color.web("#888899"));
        Label duration = new Label(movie.getDurationFormatted());
        duration.getStyleClass().add("movie-info");
        durationBox.getChildren().addAll(clockIcon, duration);

        HBox genreBox = new HBox(5);
        genreBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon genreIcon = new FontIcon("fas-tags");
        genreIcon.setIconSize(12);
        genreIcon.setIconColor(javafx.scene.paint.Color.web("#888899"));

        String genreText = limitText(String.join(", ", movie.getGenres()), 20);

        Label genre = new Label(genreText);
        genre.getStyleClass().add("movie-info");
        genre.setWrapText(false);
        genre.setMaxWidth(190);

        genreBox.getChildren().addAll(genreIcon, genre);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon starIcon = new FontIcon("fas-star");
        starIcon.setIconSize(12);
        starIcon.setIconColor(javafx.scene.paint.Color.web("#ffaa33"));
        Label rating = new Label(movie.getRatingFormatted());
        rating.getStyleClass().add("movie-rating");
        ratingBox.getChildren().addAll(starIcon, rating);

        Button bookButton = new Button("ĐẶT VÉ");
        bookButton.getStyleClass().add("book-ticket-button");
        bookButton.setPrefSize(220, 40);
        bookButton.setOnAction(e -> handleBookTicket(movie));

        infoBox.getChildren().addAll(title, durationBox, genreBox, ratingBox, bookButton);
        card.getChildren().addAll(poster, infoBox);

        card.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Button) &&
                    !isDescendantOfButton(e.getTarget())) {
                handleMovieClick(movie);
            }
        });

        return card;
    }

    private String limitText(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    // Tạo card cho phim sắp ra mắt
    private VBox createComingSoonCard(Movie movie) {

        VBox card = new VBox(12);
        card.getStyleClass().add("movie-card");

        // ===============================
        // POSTER (CÓ ẢNH + OVERLAY + FALLBACK)
        // ===============================
        StackPane poster = new StackPane();
        poster.setPrefSize(220, 320);

        String imageUrl = movie.getPosterUrl();

        // Set ảnh nền
        poster.setStyle(
                "-fx-background-image: url('" + imageUrl + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;");

        // Overlay mờ
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.25);");
        overlay.setMouseTransparent(true);

        // FALLBACK (nếu ảnh lỗi)
        VBox posterContent = new VBox(10);
        posterContent.setAlignment(Pos.CENTER);
        posterContent.setPadding(new Insets(20));

        Label comingLabel = new Label("Sắp ra mắt");
        comingLabel.setStyle(
                "-fx-text-fill: #66b3ff; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px;");

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle(
                "-fx-text-fill: #cccccc; " +
                        "-fx-font-size: 13px; " +
                        "-fx-wrap-text: true; " +
                        "-fx-text-alignment: center;");
        titleLabel.setMaxWidth(180);
        titleLabel.setAlignment(Pos.CENTER);

        posterContent.getChildren().addAll(comingLabel, titleLabel);

        // Kiểm tra ảnh có load được không
        Image testImage = new Image(imageUrl, false);
        testImage.exceptionProperty().addListener((obs, old, err) -> {
            if (err != null) {
                System.out.println("Poster lỗi: " + err.getMessage());
            } else {
                poster.getChildren().remove(posterContent); // ảnh OK → bỏ fallback
            }
        });

        // Add overlay + fallback vào poster
        poster.getChildren().addAll(overlay, posterContent);

        // ===============================
        // INFO SECTION
        // ===============================
        VBox infoBox = new VBox(8);

        // Tên phim
        Label title = new Label(movie.getTitle());
        title.getStyleClass().add("movie-title");
        title.setWrapText(true);
        title.setMaxWidth(220);

        // Ngày chiếu
        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon calendarIcon = new FontIcon("fas-calendar");
        calendarIcon.setIconSize(12);
        calendarIcon.setIconColor(javafx.scene.paint.Color.web("#888899"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label releaseDate = new Label("Khởi chiếu: " + movie.getReleaseDate().format(formatter));
        releaseDate.getStyleClass().add("movie-info");

        dateBox.getChildren().addAll(calendarIcon, releaseDate);

        // Thể loại
        HBox genreBox = new HBox(5);
        genreBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon genreIcon = new FontIcon("fas-tags");
        genreIcon.setIconSize(12);
        genreIcon.setIconColor(javafx.scene.paint.Color.web("#888899"));

        String genreText = limitText(String.join(", ", movie.getGenres()), 20);

        Label genre = new Label(genreText);
        genre.getStyleClass().add("movie-info");
        genre.setWrapText(false);
        genre.setMaxWidth(190);

        genreBox.getChildren().addAll(genreIcon, genre);

        // Thời lượng
        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon clockIcon = new FontIcon("fas-clock");
        clockIcon.setIconSize(12);
        clockIcon.setIconColor(javafx.scene.paint.Color.web("#888899"));

        Label duration = new Label(movie.getDurationFormatted());
        duration.getStyleClass().add("movie-info");

        durationBox.getChildren().addAll(clockIcon, duration);

        // Nút đặt trước
        Button preBookButton = new Button("ĐẶT TRƯỚC");
        preBookButton.getStyleClass().add("book-ticket-button-secondary");
        preBookButton.setPrefSize(220, 40);
        preBookButton.setOnAction(e -> handlePreBookTicket(movie));

        // Gắn phần thông tin vào card
        infoBox.getChildren().addAll(title, dateBox, genreBox, durationBox, preBookButton);
        card.getChildren().addAll(poster, infoBox);

        // Click vào card để xem chi tiết
        card.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Button) &&
                    !isDescendantOfButton(e.getTarget())) {
                handleMovieClick(movie);
            }
        });

        return card;
    }

    // Helper method để check xem có phải click vào button không
    private boolean isDescendantOfButton(Object node) {
        if (node instanceof javafx.scene.Node) {
            javafx.scene.Node current = (javafx.scene.Node) node;
            while (current != null) {
                if (current instanceof Button) {
                    return true;
                }
                current = current.getParent();
            }
        }
        return false;
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            System.out.println("Vui lòng nhập từ khóa tìm kiếm");
            return;
        }
        System.out.println("Tìm kiếm: " + query);
        // TODO: Implement search logic
        // Có thể filter movies và hiển thị lại
    }

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

    private void handleMovieClick(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/movie-detail.fxml"));
            Parent newRoot = loader.load();

            MovieDetailController controller = loader.getController();
            Movie selectedMovie = findMovieById(movie.getId());
            if (selectedMovie != null) {
                controller.setMovieData(selectedMovie);
            }

            Stage stage = (Stage) nowShowingContainer.getScene().getWindow();
            Scene currentScene = stage.getScene();

            boolean wasFullScreen = stage.isFullScreen();

            // TRỌNG TÂM: Chỉ thay Root, KHÔNG thay Scene
            currentScene.setRoot(newRoot);

            // Giữ nguyên fullscreen mà không bị nháy
            if (wasFullScreen) {
                // Dùng runLater để chắc chắn root đã được áp dụng xong
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint(""); // ẩn dòng "Press ESC to exit full screen"
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBookTicket(Movie movie) {
        System.out.println("=== ĐẶT VÉ ===");
        System.out.println("Đặt vé cho phim: " + movie.getTitle());
        System.out.println("Chuyển đến trang chọn suất chiếu...");
        System.out.println("===============");
        // TODO: Navigate to booking page with movie info
    }

    private void handlePreBookTicket(Movie movie) {
        System.out.println("=== ĐẶT TRƯỚC VÉ ===");
        System.out.println("Đặt trước vé cho phim: " + movie.getTitle());
        System.out.println("Ngày khởi chiếu: " + movie.getReleaseDate());
        System.out.println("Chuyển đến trang đặt trước...");
        System.out.println("====================");
        // TODO: Navigate to pre-booking page
    }

    private Movie findMovieById(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            return null;
        }

        // Tìm trong phim đang chiếu trước
        Movie found = nowShowingMovies.stream()
                .filter(m -> movieId.equals(m.getId()))
                .findFirst()
                .orElse(null);

        // Nếu không có → tìm trong phim sắp chiếu
        if (found == null) {
            found = comingSoonMovies.stream()
                    .filter(m -> movieId.equals(m.getId()))
                    .findFirst()
                    .orElse(null);
        }

        return found;
    }

    @FXML
    private void handleHome() {
        System.out.println("Navigate to Home");
        initialize();
    }

    @FXML
    private void handleCinemas() {
        System.out.println("Navigate to Cinemas");
        showAlert("Thông báo", "Trang rạp đang được phát triển");
    }

    @FXML
    private void handleSchedule() {
        System.out.println("Navigate to Schedule");
        showAlert("Thông báo", "Trang lịch chiếu đang được phát triển");
    }
    // ==================== HELPER METHOD ====================

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}