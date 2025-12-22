package com.cinema.controllers.admin.movie;

import com.cinema.models.Movie;
import com.cinema.models.Genre;
import com.cinema.models.Movie.MovieStatus;
import com.cinema.utils.admin.MovieApi;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MovieListController implements Initializable {

    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, String> titleCol;
    @FXML private TableColumn<Movie, String> genresCol;
    @FXML private TableColumn<Movie, Integer> durationCol;
    @FXML private TableColumn<Movie, String> releaseCol;
    @FXML private TableColumn<Movie, String> statusCol;
    @FXML private TableColumn<Movie, String> ageCol;
    @FXML private TableColumn<Movie, String> languageCol;
    @FXML private TableColumn<Movie, Double> ratingCol;
    @FXML private TableColumn<Movie, Void> actionCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> ageFilter;
    @FXML private ComboBox<String> genreFilter;
    
    @FXML private Label totalMoviesLabel;
    @FXML private Label nowShowingLabel;
    @FXML private Label comingSoonLabel;

    private ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private ObservableList<Movie> filteredList = FXCollections.observableArrayList();

    private MovieApi movieService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        movieService = new MovieApi();

        setupColumns();
        setupFilters();
        setupActions();
        loadMoviesFromAPI();
    }

    private void setupColumns() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("ageRating"));
        languageCol.setCellValueFactory(new PropertyValueFactory<>("language"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        // Genres column
        genresCol.setCellValueFactory(cell -> {
            if (cell.getValue().getGenres() != null && !cell.getValue().getGenres().isEmpty()) {
                String genreNames = cell.getValue().getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.joining(", "));
                return new ReadOnlyStringWrapper(genreNames);
            }
            return new ReadOnlyStringWrapper("-");
        });

        // Release date column
        releaseCol.setCellValueFactory(cell -> 
            new ReadOnlyStringWrapper(
                cell.getValue().getReleaseDate() != null
                    ? cell.getValue().getReleaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "-"
            )
        );

        // Status column
        statusCol.setCellValueFactory(cell -> {
            if (cell.getValue().getStatus() != null) {
                String statusText = "";
                switch (cell.getValue().getStatus()) {
                    case NOW_SHOWING: statusText = "Đang chiếu"; break;
                    case COMING_SOON: statusText = "Sắp chiếu"; break;
                }
                return new ReadOnlyStringWrapper(statusText);
            }
            return new ReadOnlyStringWrapper("-");
        });

        movieTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        movieTable.setItems(filteredList);
    }

    private void setupActions() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Xem");
            private final Button editBtn = new Button("Sửa");
            private final Button delBtn = new Button("Xóa");
            private final HBox box = new HBox(6, viewBtn, editBtn, delBtn);

            {
                viewBtn.getStyleClass().add("btn-view");
                editBtn.getStyleClass().add("btn-edit");
                delBtn.getStyleClass().add("btn-delete");

                viewBtn.setOnAction(e -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    if (movie != null) viewMovie(movie);
                });
                
                editBtn.setOnAction(e -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    if (movie != null) editMovie(movie);
                });
                
                delBtn.setOnAction(e -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    if (movie != null) deleteMovie(movie);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("Tất cả", "Đang chiếu", "Sắp chiếu");
        ageFilter.getItems().addAll("Tất cả", "P", "C13", "C16", "C18");
        genreFilter.getItems().addAll("Tất cả", "Hành động", "Kinh dị", "Hài", "Tình cảm", "Khoa học viễn tưởng");
        
        statusFilter.setValue("Tất cả");
        ageFilter.setValue("Tất cả");
        genreFilter.setValue("Tất cả");
    }

    /**
     * Load movies from API
     */
    private void loadMoviesFromAPI() {
        VBox loadingPlaceholder = new VBox(10);
        loadingPlaceholder.setAlignment(Pos.CENTER);
        loadingPlaceholder.setStyle("-fx-padding: 40;");
        Label loadingLabel = new Label("Đang tải dữ liệu...");
        loadingPlaceholder.getChildren().add(loadingLabel);
        movieTable.setPlaceholder(loadingPlaceholder);
        
        movieService.getAllMovies()
            .thenAccept(movies -> {
                Platform.runLater(() -> {
                    movieList.setAll(movies);
                    filteredList.setAll(movies);
                    updateSummary();
                    
                    if (movies.isEmpty()) {
                        movieTable.setPlaceholder(createEmptyPlaceholder());
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Không thể tải danh sách phim: " + ex.getMessage());
                    Label errorLabel = new Label("Lỗi khi tải dữ liệu");
                    errorLabel.setStyle("-fx-text-fill: red;");
                    movieTable.setPlaceholder(errorLabel);
                });
                ex.printStackTrace();
                return null;
            });
    }

    private VBox createEmptyPlaceholder() {
        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-padding: 40;");
        
        Label icon = new Label("🎬");
        icon.setStyle("-fx-font-size: 48px;");
        
        Label text1 = new Label("Chưa có phim nào");
        text1.getStyleClass().add("placeholder-text");
        
        Label text2 = new Label("Nhấn 'Thêm Phim Mới' để bắt đầu");
        text2.getStyleClass().add("placeholder-hint");
        
        placeholder.getChildren().addAll(icon, text1, text2);
        return placeholder;
    }

    @FXML
    private void searchMovies() {
        String keyword = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        String age = ageFilter.getValue();
        String genre = genreFilter.getValue();

        filteredList.clear();

        for (Movie movie : movieList) {
            boolean matchKeyword = keyword.isEmpty() || 
                movie.getTitle().toLowerCase().contains(keyword);
            
            boolean matchStatus = status.equals("Tất cả") || 
                getStatusText(movie.getStatus()).equals(status);
            
            boolean matchAge = age.equals("Tất cả") || 
                (movie.getAgeRating() != null && movie.getAgeRating().equals(age));
            
            boolean matchGenre = genre.equals("Tất cả") || 
                (movie.getGenres() != null && movie.getGenres().stream()
                    .anyMatch(g -> g.getName().equals(genre)));

            if (matchKeyword && matchStatus && matchAge && matchGenre) {
                filteredList.add(movie);
            }
        }
        
        updateSummary();
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        statusFilter.setValue("Tất cả");
        ageFilter.setValue("Tất cả");
        genreFilter.setValue("Tất cả");
        filteredList.setAll(movieList);
        updateSummary();
    }

    private String getStatusText(MovieStatus status) {
        if (status == null) return "-";
        switch (status) {
            case NOW_SHOWING: return "Đang chiếu";
            case COMING_SOON: return "Sắp chiếu";
            default: return "-";
        }
    }

    private void updateSummary() {
        int total = filteredList.size();
        long nowShowing = filteredList.stream()
            .filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING)
            .count();
        long comingSoon = filteredList.stream()
            .filter(m -> m.getStatus() == MovieStatus.COMING_SOON)
            .count();

        totalMoviesLabel.setText("Tổng số phim: " + total);
        nowShowingLabel.setText("Đang chiếu: " + nowShowing);
        comingSoonLabel.setText("Sắp chiếu: " + comingSoon);
    }

    /**
     * Open Add Movie Form
     */
    @FXML
    private void openAddMovie() {
        openMovieForm(null);
    }

    /**
     * View Movie Details
     */
    private void viewMovie(Movie movie) {
        String info = String.format(
            "Tên phim: %s\n" +
            "Thể loại: %s\n" +
            "Thời lượng: %d phút\n" +
            "Ngày phát hành: %s\n" +
            "Trạng thái: %s\n" +
            "Độ tuổi: %s\n" +
            "Ngôn ngữ: %s\n" +
            "Đánh giá: %.1f/5.0",
            movie.getTitle(),
            movie.getGenres() != null ? 
                movie.getGenres().stream().map(g -> g.getName()).collect(Collectors.joining(", ")) : "-",
            movie.getDuration(),
            movie.getReleaseDate() != null ? 
                movie.getReleaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
            getStatusText(movie.getStatus()),
            movie.getAgeRating(),
            movie.getLanguage(),
            movie.getAverageRating()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết phim");
        alert.setHeaderText(movie.getTitle());
        alert.setContentText(info);
        
        // Make alert resizable for long descriptions
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(500);
        
        alert.showAndWait();
    }

    /**
     * Edit Movie
     */
    private void editMovie(Movie movie) {
        openMovieForm(movie);
    }

    /**
     * Open Movie Form (Create or Edit)
     */
    private void openMovieForm(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/admin/partials/movie/movie-form-content.fxml"));
            Parent root = loader.load();

            MovieFormController controller = loader.getController();
            controller.setMovie(movie);
            
            // Set callback to refresh data after save
            controller.setOnSaveCallback((savedMovie) -> {
                if (movie == null) {
                    // New movie created
                    handleMovieCreated(savedMovie);
                } else {
                    // Existing movie updated
                    handleMovieUpdated(savedMovie);
                }
            });

            Stage stage = new Stage();
            stage.setTitle(movie == null ? "Thêm Phim Mới" : "Chỉnh Sửa Phim");
            stage.setScene(new Scene(root, 700, 800));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(movieTable.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở form: " + e.getMessage());
        }
    }

    /**
     * Handle Movie Created
     */
    private void handleMovieCreated(Movie newMovie) {
        movieService.createMovie(newMovie)
            .thenAccept(createdMovie -> {
                Platform.runLater(() -> {
                    // Add to list
                    movieList.add(0, createdMovie); // Add at beginning
                    
                    // Reapply filters
                    searchMovies();
                    
                    showSuccess("Đã thêm phim thành công!");
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Không thể thêm phim: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    /**
     * Handle Movie Updated
     */
    private void handleMovieUpdated(Movie updatedMovie) {
        movieService.updateMovie(updatedMovie.getId(), updatedMovie)
            .thenAccept(movie -> {
                Platform.runLater(() -> {
                    // Find and replace in list
                    for (int i = 0; i < movieList.size(); i++) {
                        if (movieList.get(i).getId().equals(movie.getId())) {
                            movieList.set(i, movie);
                            break;
                        }
                    }
                    
                    // Reapply filters
                    searchMovies();
                    
                    showSuccess("Đã cập nhật phim thành công!");
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Không thể cập nhật phim: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    /**
     * Delete Movie
     */
    private void deleteMovie(Movie movie) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận Xóa");
        confirmAlert.setHeaderText("Bạn có chắc muốn xóa phim này?");
        confirmAlert.setContentText(
            "Phim: " + movie.getTitle() + "\n\n" +
            "Cảnh báo: Hành động này không thể hoàn tác!"
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Show loading
                Label loadingLabel = new Label("Đang xóa...");
                loadingLabel.setStyle("-fx-text-fill: #666;");
                movieTable.setPlaceholder(loadingLabel);
                
                movieService.deleteMovie(movie.getId())
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            // Remove from lists
                            movieList.remove(movie);
                            filteredList.remove(movie);
                            
                            updateSummary();
                            
                            // Restore placeholder if empty
                            if (movieList.isEmpty()) {
                                movieTable.setPlaceholder(createEmptyPlaceholder());
                            }
                            
                            showSuccess("Đã xóa phim thành công!");
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            // Check if it's a constraint violation (has related data)
                            if (ex.getMessage().contains("CONSTRAINT") || 
                                ex.getMessage().contains("foreign key")) {
                                showError(
                                    "Không thể xóa phim!\n\n" +
                                    "Phim này có dữ liệu liên quan (lịch chiếu, đặt vé, bình luận...).\n" +
                                    "Vui lòng xóa các dữ liệu liên quan trước."
                                );
                            } else {
                                showError("Không thể xóa phim: " + ex.getMessage());
                            }
                            
                            // Restore normal placeholder
                            if (!movieList.isEmpty()) {
                                movieTable.setPlaceholder(null);
                            }
                        });
                        ex.printStackTrace();
                        return null;
                    });
            }
        });
    }

    /**
     * Refresh Data from API
     */
    private void refreshData() {
        loadMoviesFromAPI();
    }

    /**
     * Show Error Alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setResizable(true);
        alert.showAndWait();
    }

    /**
     * Show Success Alert
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}