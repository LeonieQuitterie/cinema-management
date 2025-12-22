package com.cinema.controllers.cinema;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;

import com.cinema.models.Actor;
import com.cinema.models.Comment;
import com.cinema.models.CommentReaction;
import com.cinema.models.Genre;
import com.cinema.models.Movie;
import com.cinema.utils.ApiClient;
import com.cinema.utils.MovieApi;
import com.cinema.utils.RatingStatsResponse;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.application.Platform;

public class MovieDetailController {

    @FXML
    private ImageView posterImage;
    @FXML
    private Label movieTitle;
    @FXML
    private Label movieTitleOriginal;
    @FXML
    private HBox genresContainer;
    @FXML
    private Label ageRatingNumber;
    @FXML
    private Label ageRatingDescription;
    @FXML
    private Label releaseDate;
    @FXML
    private Label duration;
    @FXML
    private Label language;
    @FXML
    private Button likeButton;
    @FXML
    private Button trailerButton;
    @FXML
    private Button buyTicketButton;
    @FXML
    private TextField searchField;
    // Rating Section
    @FXML
    private Label overallRating;
    @FXML
    private Label totalReviews;
    @FXML
    private VBox ratingBarsContainer;

    // Synopsis
    @FXML
    private Label synopsisShort;
    @FXML
    private Label synopsisFullContainer;
    @FXML
    private Button expandButton;

    // Cast
    @FXML
    private FlowPane castGrid;

    // Comments
    @FXML
    private HBox ratingStarsInput;
    @FXML
    private Label ratingHint; // Thêm dòng này

    @FXML
    private TextArea commentInput;

    @FXML
    private Button postCommentButton;
    @FXML
    private VBox commentsList;
    @FXML
    private Button loadMoreCommentsButton;

    @FXML
    private HBox nowShowingContainer;

    private boolean isSynopsisExpanded = false;
    private boolean isSpoilerChecked = false;
    private int currentRating = 0;

    private String currentMovieId; // Thêm dòng này (lưu ID phim đang chọn vé)

    @FXML
    private Button homeButton; // fx:id phải khớp

    private final IntegerProperty currentRatingProperty = new SimpleIntegerProperty(0);
    private String currentUserId = ApiClient.getCurrentUserId();

    private static class IconPair {
        FontIcon empty;
        FontIcon full;

        IconPair(FontIcon empty, FontIcon full) {
            this.empty = empty;
            this.full = full;
        }
    }

    @FXML
    public void initialize() {
        setupEventHandlers();
        setupRatingStarsInput();
        // Không load dữ liệu cứng nữa
        // Dữ liệu sẽ được inject qua setMovieData() từ CustomerHomeController
    }

    // Method được gọi từ CustomerHomeController khi người dùng click vào 1 phim
    public void loadMovieById(String movieId) {
        this.currentMovieId = movieId;
        // Không gọi loadMovieData() nữa để tránh ghi đè dữ liệu thật
    }

    // Method chính để hiển thị dữ liệu phim thực tế
    public void setMovieData(Movie movie) {
        this.currentMovieId = movie.getId(); // ← THÊM DÒNG NÀY

        movieTitle.setText(movie.getTitle());
        movieTitleOriginal.setText(movie.getTitle() + " (" + movie.getReleaseDate().getYear() + ")");

        // Poster
        try {
            posterImage.setImage(new Image(movie.getPosterUrl()));
        } catch (Exception e) {
            e.printStackTrace();
            // Có thể set ảnh mặc định nếu cần
            // posterImage.setImage(new Image("path/to/default_poster.jpg"));
        }

        // Genres
        loadGenres(movie.getGenres().stream().map(Genre::getName).toArray(String[]::new));

        // Age Rating
        ageRatingNumber.setText(movie.getAgeRating());
        ageRatingDescription.setText(movie.getAgeRatingDescription());

        // Details
        releaseDate.setText(movie.getReleaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        duration.setText(movie.getDuration() + " phút");
        language.setText(movie.getLanguage());

        RatingStatsResponse stats = MovieApi.getRatingStats(currentMovieId);

        if (stats != null && stats.data != null) {
            overallRating.setText(
                    String.format("%.1f", stats.data.average_rating));
        }

        totalReviews.setText("10 đánh giá");

        // Synopsis
        String synopsis = movie.getDescription() != null ? movie.getDescription() : "";
        if (synopsis.length() > 200) {
            synopsisShort.setText(synopsis.substring(0, 200) + "...");
            synopsisFullContainer.setText(synopsis);
            expandButton.setVisible(true);
        } else {
            synopsisShort.setText(synopsis);
            synopsisFullContainer.setText(synopsis);
            expandButton.setVisible(false);
        }

        loadRatingBars();
        loadCast(); // tạm giữ mock
        loadComments(); // tạm giữ mock

    }

    private void setupEventHandlers() {
        expandButton.setOnAction(e -> toggleSynopsis());
        postCommentButton.setOnAction(e -> postComment());
        likeButton.setOnAction(e -> handleLike());
        trailerButton.setOnAction(e -> handleTrailer());
        buyTicketButton.setOnAction(e -> handleBuyTicket());
        setupRatingStarsInput();
    }

    // === Các method giữ nguyên (mock data tạm thời) ===
    private void loadGenres(String[] genres) {
        genresContainer.getChildren().clear();
        for (String genre : genres) {
            Label genreLabel = new Label(genre);
            genreLabel.getStyleClass().add("genre-tag");
            genresContainer.getChildren().add(genreLabel);
        }
    }

    private void loadRatingBars() {
        // Xóa các bar cũ nếu có (tránh trùng khi reload)
        ratingBarsContainer.getChildren().clear();

        // Gọi API lấy thống kê đánh giá theo movieId
        RatingStatsResponse stats = MovieApi.getRatingStats(currentMovieId);

        if (stats == null || stats.data == null) {
            // Nếu lỗi hoặc chưa có đánh giá → hiển thị mặc định 0
            for (int i = 5; i >= 1; i--) {
                HBox ratingRow = createRatingBar(i, 0.0, "0", "#cccccc");
                ratingBarsContainer.getChildren().add(ratingRow);
            }
            totalReviews.setText("0 đánh giá");
            return;
        }

        RatingStatsResponse.RatingStatsData data = stats.data;

        // Backend trả về: counts[0] = 1 sao, counts[4] = 5 sao
        // percentages[0] = 1 sao, percentages[4] = 5 sao
        int total = data.total_ratings;

        // Cập nhật tổng số đánh giá
        if (total == 0) {
            totalReviews.setText("Chưa có đánh giá");
        } else {
            totalReviews.setText(String.format("%,d đánh giá", total).replace(",", "."));
        }

        // Mảng màu từ 5 sao xuống 1 sao (đẹp dần từ xanh → đỏ)
        String[] colors = { "#88ff88", "#ffcc66", "#ffa366", "#ff6b6b", "#ff3366" };

        // Duyệt từ 5 sao xuống 1 sao
        for (int i = 4; i >= 0; i--) { // i=4 → 5 sao, i=0 → 1 sao
            double percentage = total == 0 ? 0 : data.percentages[i] * 100.0;
            String countStr = data.counts[i];

            // Format số: 1200 → 1.2K, 15000 → 15K
            String displayCount = formatCount(countStr);

            HBox ratingRow = createRatingBar(5 - i, percentage, displayCount, colors[i]);
            ratingBarsContainer.getChildren().add(ratingRow);
        }
    }

    private String formatCount(String countStr) {
        try {
            long count = Long.parseLong(countStr);
            if (count >= 1000000) {
                return String.format("%.1fM", count / 1000000.0).replace(".0M", "M");
            } else if (count >= 1000) {
                return String.format("%.1fK", count / 1000.0).replace(".0K", "K");
            } else {
                return String.format("%,d", count).replace(",", ".");
            }
        } catch (Exception e) {
            return countStr;
        }
    }

    private HBox createRatingBar(int stars, double percentage, String count, String color) {
        HBox row = new HBox(15);
        row.getStyleClass().add("rating-bar-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label starLabel = new Label(stars + " star");
        starLabel.getStyleClass().add("rating-star-label");

        VBox barContainer = new VBox();
        barContainer.setPrefHeight(12);
        barContainer.setPrefWidth(400);

        HBox barBg = new HBox();
        barBg.setPrefHeight(12);
        barBg.setStyle("-fx-background-color: #1a2a4a; -fx-background-radius: 6;");

        Region barFill = new Region();
        barFill.setPrefWidth(400 * percentage);
        barFill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");

        barBg.getChildren().add(barFill);
        barContainer.getChildren().add(barBg);

        Label percentLabel = new Label(String.format("%.0f%%", percentage));
        percentLabel.getStyleClass().add("rating-percentage");

        Label countLabel = new Label("(" + count + ")");
        countLabel.getStyleClass().add("rating-count");

        row.getChildren().addAll(starLabel, barContainer, percentLabel, countLabel);
        return row;
    }

    private void loadCast() {
        castGrid.getChildren().clear();

        // Gọi API lấy danh sách diễn viên thật → trả về List<Actor>
        List<Actor> castList = MovieApi.getMovieCast(currentMovieId);

        if (castList == null || castList.isEmpty()) {
            Label noDataLabel = new Label("Chưa có thông tin diễn viên");
            noDataLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic; -fx-padding: 20;");
            castGrid.getChildren().add(noDataLabel);
            return;
        }

        for (Actor actor : castList) {
            String realName = actor.getRealName();
            String characterName = actor.getCharacter();
            String imageUrl = actor.getImageUrl(); // ← Lấy ảnh

            castGrid.getChildren().add(createCastCard(realName, characterName, imageUrl));
        }
    }

    // Cast Card
    private VBox createCastCard(String actorName, String characterName, String imageUrl) {
        VBox card = new VBox(10);
        card.getStyleClass().add("cast-card");
        card.setAlignment(Pos.CENTER);

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("cast-image-container");
        imageContainer.setPrefSize(140, 180);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(140);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("cast-image");

        // ✅ Load ảnh từ DB
        if (imageUrl != null && !imageUrl.isBlank()) {
            imageView.setImage(new Image(imageUrl, true));
        } else {
            imageView.setImage(
                    new Image(
                            getClass()
                                    .getResource("/images/default-avatar.png")
                                    .toExternalForm(),
                            true));
        }

        Rectangle clip = new Rectangle(140, 180);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageView.setClip(clip);

        imageContainer.getChildren().add(imageView);

        Label actorLabel = new Label(actorName);
        actorLabel.getStyleClass().add("actor-name");
        actorLabel.setWrapText(true);
        actorLabel.setMaxWidth(140);

        Label characterLabel = new Label(characterName);
        characterLabel.getStyleClass().add("character-name");
        characterLabel.setWrapText(true);
        characterLabel.setMaxWidth(140);

        card.getChildren().addAll(imageContainer, actorLabel, characterLabel);
        return card;
    }

    private void setupRatingStarsInput() {
        ratingStarsInput.getChildren().clear();

        for (int i = 1; i <= 5; i++) {
            final int rating = i;

            // Icon sao rỗng (outline) và sao đầy (vàng)
            FontIcon starEmpty = new FontIcon("far-star"); // sao viền
            FontIcon starFull = new FontIcon("fas-star"); // sao đầy

            starEmpty.setIconSize(20); // Size của icon
            starFull.setIconSize(20);
            starEmpty.setIconColor(javafx.scene.paint.Color.web("#888899")); // xám
            starFull.setIconColor(javafx.scene.paint.Color.web("#ffaa33")); // vàng

            // StackPane để chồng 2 icon lên nhau
            StackPane starPane = new StackPane(starEmpty, starFull);
            starPane.setAlignment(Pos.CENTER);
            starPane.setPrefSize(32, 32);
            starPane.setCursor(javafx.scene.Cursor.HAND);

            // Ban đầu chỉ hiện sao rỗng
            starFull.setVisible(false);

            // Hover: hiện preview số sao sẽ chọn
            starPane.setOnMouseEntered(e -> highlightStars(rating));

            // Rời chuột: quay lại trạng thái đã chọn
            starPane.setOnMouseExited(e -> highlightStars(currentRating));

            // Click: chọn số sao
            starPane.setOnMouseClicked(e -> {
                currentRating = rating;
                updateRating(rating);
            });

            // Lưu cặp icon để dùng sau
            starPane.setUserData(new IconPair(starEmpty, starFull));

            ratingStarsInput.getChildren().add(starPane);
        }
    }

    private void updateRating(int rating) {
        currentRating = rating;
        currentRatingProperty.set(rating);
        applyStarState(); // vẽ lại sao đã chọn
    }

    // Áp dụng trạng thái sao đã chọn (không hover)
    private void applyStarState() {
        highlightStars(0); // 0 nghĩa là không hover
    }

    // Hiệu ứng hover + trạng thái chọn
    private void highlightStars(int hoverRating) {
        for (int i = 0; i < 5; i++) {
            StackPane pane = (StackPane) ratingStarsInput.getChildren().get(i);
            IconPair pair = (IconPair) pane.getUserData();

            boolean shouldFill = (i < hoverRating) || (hoverRating == 0 && i < currentRating);

            pair.empty.setVisible(!shouldFill);
            pair.full.setVisible(shouldFill);

            // Màu khi hover (vàng nhạt) hoặc đã chọn (vàng đậm)
            if (i < hoverRating) {
                pair.full.setIconColor(javafx.scene.paint.Color.web("#ffcc00")); // hover
            } else if (i < currentRating) {
                pair.full.setIconColor(javafx.scene.paint.Color.web("#ffaa33")); // đã chọn
            }
        }
    }

    private void loadComments() {
        commentsList.getChildren().clear();

        if (currentMovieId == null || currentMovieId.isBlank())
            return;

        new Thread(() -> {
            List<Comment> comments = MovieApi.getMovieComments(currentMovieId);

            Platform.runLater(() -> {
                for (Comment comment : comments) {
                    commentsList.getChildren().add(createCommentItem(comment));
                }
            });
        }).start();
    }

    private VBox createCommentItem(Comment comment) {
        VBox item = new VBox(12);
        item.getStyleClass().add("comment-item");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(42);
        avatarView.setFitHeight(42);
        avatarView.setPreserveRatio(false);

        String avatarUrl = comment.getUserAvatar();
        String fallbackUrl = "https://i.pinimg.com/1200x/b1/7c/cf/b17ccf72f102b9284db304597db9863e.jpg";

        Image avatarImg;
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            avatarImg = new Image(avatarUrl, true);
        } else {
            avatarImg = new Image(fallbackUrl, true);
        }

        avatarImg.errorProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                avatarView.setImage(new Image(fallbackUrl, true));
            }
        });

        avatarView.setImage(avatarImg);
        avatarView.setClip(new Circle(21, 21, 21));

        VBox userInfo = new VBox(2);
        Label userLabel = new Label(comment.getUserName());
        userLabel.getStyleClass().add("username");
        Label timeLabel = new Label(comment.getFormattedTime());
        timeLabel.getStyleClass().add("comment-time");
        userInfo.getChildren().addAll(userLabel, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        FontIcon starIcon = new FontIcon("fas-star");
        starIcon.setIconSize(18);
        starIcon.setIconColor(javafx.scene.paint.Color.web("#FFD700"));
        Label ratingLabel = new Label(comment.getRating() + "/5");
        ratingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        HBox ratingBox = new HBox(6, starIcon, ratingLabel);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(avatarView, userInfo, spacer, ratingBox);

        // Nội dung
        Label contentLabel = new Label(comment.getContent());
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);

        // Reaction bar
        HBox reactionSummaryBar = new HBox(15);
        reactionSummaryBar.setAlignment(Pos.CENTER_LEFT);
        reactionSummaryBar.setPadding(new Insets(8, 0, 0, 0));

        HBox topReactionsBox = new HBox(4);
        topReactionsBox.setAlignment(Pos.CENTER_LEFT);

        Label totalReactionsLabel = new Label(String.valueOf(comment.getTotalReactions()));
        totalReactionsLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 13px;");

        // Hiển thị top 3 reactions ban đầu
        for (String reactionType : comment.getTop3Reactions()) {
            FontIcon icon = new FontIcon(getIconLiteral(reactionType));
            icon.setIconSize(18);
            icon.setIconColor(javafx.scene.paint.Color.web(getReactionColor(reactionType)));
            topReactionsBox.getChildren().add(icon);
        }

        HBox leftSummary = new HBox(6, topReactionsBox, totalReactionsLabel);
        leftSummary.setAlignment(Pos.CENTER_LEFT);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox likeButton = createLikeButton(comment, topReactionsBox, totalReactionsLabel);

        HBox actionButtons = new HBox(20, likeButton);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        reactionSummaryBar.getChildren().addAll(leftSummary, rightSpacer, actionButtons);

        item.getChildren().addAll(header, contentLabel, reactionSummaryBar);
        return item;
    }

    private HBox createLikeButton(Comment comment, HBox topReactionsBox, Label totalReactionsLabel) {
        HBox likeButton = new HBox(6);
        likeButton.setAlignment(Pos.CENTER);
        likeButton.getStyleClass().add("action-button");
        likeButton.setCursor(Cursor.HAND);

        FontIcon likeIcon = new FontIcon("far-thumbs-up");
        likeIcon.setIconSize(16);
        likeIcon.setIconColor(javafx.scene.paint.Color.web("#b0b0b0"));

        Label likeCountLabel = new Label("0");
        likeCountLabel.getStyleClass().add("action-label");

        likeButton.getChildren().addAll(likeIcon, likeCountLabel);

        // Lấy reaction hiện tại của user
        String userReaction = comment.getUserReaction(currentUserId);
        String[] currentUserReaction = { userReaction };

        // Map để đếm reactions (tính từ comment.getReactions())
        Map<String, Integer> reactionCountMap = new HashMap<>(comment.getReactions());

        // Cập nhật UI ban đầu dựa vào reaction của user
        if (userReaction != null) {
            likeIcon.setIconLiteral(getIconLiteral(userReaction));
            likeIcon.setIconColor(javafx.scene.paint.Color.web(getReactionColor(userReaction)));
            likeCountLabel.setText("1");
        }

        // Cập nhật top 3 + tổng
        Runnable updateSummary = () -> {
            int total = reactionCountMap.values().stream().mapToInt(Integer::intValue).sum();
            totalReactionsLabel.setText(total > 0 ? String.valueOf(total) : "0");

            topReactionsBox.getChildren().clear();
            reactionCountMap.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(3)
                    .forEach(e -> {
                        FontIcon icon = new FontIcon(getIconLiteral(e.getKey()));
                        icon.setIconSize(18);
                        icon.setIconColor(javafx.scene.paint.Color.web(getReactionColor(e.getKey())));
                        topReactionsBox.getChildren().add(icon);
                    });
        };

        // Cập nhật nút Like
        Runnable updateLikeButton = () -> {
            if (currentUserReaction[0] == null) {
                likeIcon.setIconLiteral("far-thumbs-up");
                likeIcon.setIconColor(javafx.scene.paint.Color.web("#b0b0b0"));
                likeCountLabel.setText("0");
            } else {
                likeIcon.setIconLiteral(getIconLiteral(currentUserReaction[0]));
                likeIcon.setIconColor(javafx.scene.paint.Color.web(getReactionColor(currentUserReaction[0])));
                likeCountLabel.setText("1");
            }
        };

        // Long press logic (giữ nguyên)
        Timeline longPress = new Timeline(new KeyFrame(Duration.millis(500),
                e -> showReactionPopup(likeButton, likeIcon, likeCountLabel, currentUserReaction,
                        reactionCountMap, updateSummary, updateLikeButton)));
        longPress.setCycleCount(1);

        likeButton.setOnMousePressed(e -> longPress.playFromStart());
        likeButton.setOnMouseReleased(e -> {
            if (longPress.getStatus() == Animation.Status.RUNNING) {
                longPress.stop();
                String current = currentUserReaction[0];

                if (current == null) {
                    // Chưa thả → thả Like
                    currentUserReaction[0] = "like";
                    reactionCountMap.merge("like", 1, Integer::sum);
                } else {
                    // Đang thả (bất kỳ loại nào) → bỏ luôn
                    reactionCountMap.merge(current, -1, Integer::sum);
                    reactionCountMap.entrySet().removeIf(en -> en.getValue() <= 0);
                    currentUserReaction[0] = null;
                }
                updateLikeButton.run();
                updateSummary.run();
            }
        });

        likeButton.setOnMouseEntered(
                e -> likeButton.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;"));
        likeButton.setOnMouseExited(e -> likeButton.setStyle("-fx-background-color: transparent;"));

        return likeButton;
    }

    private void showReactionPopup(HBox likeButton, FontIcon likeIcon, Label likeCountLabel,
            String[] currentUserReaction, Map<String, Integer> reactionCountMap,
            Runnable updateSummary, Runnable updateLikeButton) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        HBox box = new HBox(10);
        box.setPadding(new Insets(8, 12, 8, 12));
        box.setStyle(
                "-fx-background-color: #2a2a2a; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 3);");
        box.setAlignment(Pos.CENTER);

        String[][] reactions = {
                { "fas-thumbs-up", "#4A90E2", "like" },
                { "fas-heart", "#E74C3C", "love" },
                { "fas-laugh-beam", "#F39C12", "haha" },
                { "fas-surprise", "#F1C40F", "wow" },
                { "fas-sad-tear", "#E67E22", "sad" },
                { "fas-angry", "#E74C3C", "angry" }
        };

        for (String[] r : reactions) {
            FontIcon icon = new FontIcon(r[0]);
            icon.setIconSize(28);
            icon.setIconColor(javafx.scene.paint.Color.web(r[1]));

            StackPane pane = new StackPane(icon);
            pane.setPrefSize(45, 45);
            pane.setCursor(Cursor.HAND);

            // SỬA TẠI ĐÂY – KHÔNG CHAIN ĐƯỢC
            pane.setOnMouseEntered(e -> {
                pane.setScaleX(1.3);
                pane.setScaleY(1.3);
            });
            pane.setOnMouseExited(e -> {
                pane.setScaleX(1.0);
                pane.setScaleY(1.0);
            });

            pane.setOnMouseClicked(e -> {
                String old = currentUserReaction[0];

                // Bỏ reaction cũ nếu có
                if (old != null) {
                    reactionCountMap.merge(old, -1, Integer::sum);
                    reactionCountMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
                }

                // Nếu click lại chính icon đang chọn → bỏ luôn, ngược lại → chọn mới
                if (r[2].equals(old)) {
                    currentUserReaction[0] = null;
                } else {
                    currentUserReaction[0] = r[2];
                    reactionCountMap.merge(r[2], 1, Integer::sum);
                }

                updateLikeButton.run();
                updateSummary.run();
                popup.hide();
            });

            box.getChildren().add(pane);
        }

        popup.getContent().add(box);
        Bounds bounds = likeButton.localToScreen(likeButton.getBoundsInLocal());
        popup.show(likeButton, bounds.getMinX() - 50, bounds.getMinY() - 70);
    }

    // Helper lấy icon + màu
    private String getIconLiteral(String type) {
        return switch (type) {
            case "like" -> "fas-thumbs-up";
            case "love" -> "fas-heart";
            case "haha" -> "fas-laugh-beam";
            case "wow" -> "fas-surprise";
            case "sad" -> "fas-sad-tear";
            case "angry" -> "fas-angry";
            default -> "fas-thumbs-up";
        };
    }

    private String getReactionColor(String type) {
        return switch (type) {
            case "like" -> "#4A90E2";
            case "love", "angry" -> "#E74C3C";
            case "haha" -> "#F39C12";
            case "wow" -> "#F1C40F";
            case "sad" -> "#E67E22";
            default -> "#4A90E2";
        };
    }

    // NÚT PHẢN HỒI (giữ nguyên)
    private HBox createReplyButton() {
        HBox btn = new HBox(6);
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add("action-button");
        btn.setCursor(Cursor.HAND);

        FontIcon icon = new FontIcon("fas-reply");
        icon.setIconSize(16);
        icon.setIconColor(javafx.scene.paint.Color.web("#b0b0b0"));

        Label lbl = new Label("Phản hồi");
        lbl.getStyleClass().add("action-label");

        btn.getChildren().addAll(icon, lbl);
        btn.setOnMouseClicked(e -> handleReply());

        btn.setOnMouseEntered(
                e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent;"));

        return btn;
    }

    // Xử lý reply
    private void handleReply() {
        // TODO: Implement reply functionality
        // Có thể show textarea để nhập reply, hoặc focus vào comment input
        System.out.println("Reply clicked");
    }

    private void toggleSynopsis() {
        isSynopsisExpanded = !isSynopsisExpanded;
        synopsisShort.setVisible(!isSynopsisExpanded);
        synopsisShort.setManaged(!isSynopsisExpanded);
        synopsisFullContainer.setVisible(isSynopsisExpanded);
        synopsisFullContainer.setManaged(isSynopsisExpanded);
        expandButton.setText(isSynopsisExpanded ? "Thu gọn left arrow" : "Xem thêm right arrow");
    }

    private void postComment() {
        if (currentRating == 0) {
            showAlert("Vui lòng chọn đánh giá!");
            return;
        }
        if (commentInput.getText().trim().isEmpty()) {
            showAlert("Vui lòng nhập bình luận!");
            return;
        }

        System.out.println("Đã đăng bình luận - Rating: " + currentRating + ", Spoiler: " + isSpoilerChecked);
        commentInput.clear();
        updateRating(0);
        currentRating = 0;
        isSpoilerChecked = false;
        showAlert("Đã đăng bình luận thành công!");
    }

    private void handleLike() {
        System.out.println("Movie liked!");
    }

    private void handleTrailer() {
        System.out.println("Play trailer");
    }

    private void handleBuyTicket() {
        try {
            // 1. Load trang showtime
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cinema/cinema-showtime.fxml"));
            Parent newRoot = loader.load();

            // 2. Gửi ID phim qua controller trang showtime
            CinemaShowTimeController controller = loader.getController();
            controller.setMovieId(currentMovieId); // ĐÚNG – dùng biến đã có

            // 3. Lấy Stage hiện tại
            Stage stage = (Stage) movieTitle.getScene().getWindow();
            Scene currentScene = stage.getScene();

            boolean wasFullScreen = stage.isFullScreen();

            // 4. Chỉ thay root (KHÔNG tạo Scene mới → không nháy)
            currentScene.setRoot(newRoot);

            // 5. Khôi phục fullscreen mượt mà
            if (wasFullScreen) {
                Platform.runLater(() -> {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint(""); // ẩn Hint
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private Movie findMovieById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findMovieById'");
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

    // private Movie findMovieById(String movieId) {
    // return mockMovies.stream()
    // .filter(m -> m.getId().equals(movieId))
    // .findFirst()
    // .orElse(null);
    // }

    @FXML
    private void handleHome(ActionEvent event) { // Thêm ActionEvent nếu chưa có
        System.out.println("Navigate to Customer Home");

        try {
            // Load FXML trang chủ khách hàng
            Parent homeRoot = FXMLLoader.load(getClass().getResource("/views/customer/customer-home.fxml"));

            // Lấy Stage hiện tại (từ bất kỳ node nào trong scene, ví dụ từ một Button hoặc
            // root)
            Stage stage = (Stage) homeButton.getScene().getWindow();
            // Thay "someNode" bằng một node thực tế trong FXML của bạn, ví dụ:
            // - Nếu có Button "Back" thì dùng button đó
            // - Hoặc dùng bất kỳ node nào có @FXML

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(homeRoot);
                stage.setScene(scene);
            } else {
                scene.setRoot(homeRoot);
            }

            // Giữ trạng thái fullscreen nếu đang bật
            if (stage.isFullScreen()) {
                stage.setFullScreen(true);
            }

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Có thể hiện thông báo lỗi cho user
            System.err.println("Không thể load trang chủ: " + e.getMessage());
        }
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