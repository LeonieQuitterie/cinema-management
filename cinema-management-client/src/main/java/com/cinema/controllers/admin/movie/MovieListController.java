package com.cinema.controllers.admin.movie;

import com.cinema.models.Movie;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.cinema.models.Movie.MovieStatus;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupFilters();
        setupActions();
        loadDummyData();
        updateSummary();
    }

    private void setupColumns() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("ageRating"));
        languageCol.setCellValueFactory(new PropertyValueFactory<>("language"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        genresCol.setCellValueFactory(cell -> {
            if (cell.getValue().getGenres() != null && !cell.getValue().getGenres().isEmpty()) {
                return new ReadOnlyStringWrapper(String.join(", ", cell.getValue().getGenres()));
            }
            return new ReadOnlyStringWrapper("-");
        });

        releaseCol.setCellValueFactory(cell -> 
            new ReadOnlyStringWrapper(
                cell.getValue().getReleaseDate() != null
                    ? cell.getValue().getReleaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "-"
            )
        );

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

                viewBtn.setOnAction(e -> viewMovie(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> editMovie(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> deleteMovie(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("Tất cả", "Đang chiếu", "Sắp chiếu", "Đã kết thúc");
        ageFilter.getItems().addAll("Tất cả", "P", "C13", "C16", "C18");
        genreFilter.getItems().addAll("Tất cả", "Hành động", "Kinh dị", "Hài", "Tình cảm", "Khoa học viễn tưởng");
        
        statusFilter.setValue("Tất cả");
        ageFilter.setValue("Tất cả");
        genreFilter.setValue("Tất cả");
    }

    private void loadDummyData() {
        // TODO: Replace with actual service/API call
        Movie movie1 = new Movie();
        movie1.setTitle("Avengers: Endgame");
        movie1.setDuration(181);
        movie1.setReleaseDate(LocalDate.of(2024, 12, 25));
        movie1.setStatus(MovieStatus.NOW_SHOWING);
        movie1.setAgeRating("C13");
        movie1.setLanguage("Tiếng Anh");
        movie1.setAverageRating(4.8);
        movie1.setGenres(Arrays.asList("Hành động", "Khoa học viễn tưởng"));

        Movie movie2 = new Movie();
        movie2.setTitle("The Conjuring 4");
        movie2.setDuration(120);
        movie2.setReleaseDate(LocalDate.of(2025, 1, 10));
        movie2.setStatus(MovieStatus.COMING_SOON);
        movie2.setAgeRating("C18");
        movie2.setLanguage("Tiếng Anh");
        movie2.setAverageRating(4.5);
        movie2.setGenres(Arrays.asList("Kinh dị"));

        movieList.addAll(movie1, movie2);
        filteredList.addAll(movieList);
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
                movie.getAgeRating().equals(age);
            
            boolean matchGenre = genre.equals("Tất cả") || 
                (movie.getGenres() != null && movie.getGenres().contains(genre));

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

    @FXML
    private void openAddMovie() {
        openMovieForm(null);
    }

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
            movie.getGenres() != null ? String.join(", ", movie.getGenres()) : "-",
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
        alert.showAndWait();
    }

    private void editMovie(Movie movie) {
        openMovieForm(movie);
    }

    private void openMovieForm(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/admin/partials/movie/movie-form-content.fxml"));
            Parent root = loader.load();

            MovieFormController controller = loader.getController();
            controller.setMovie(movie);
            controller.setOnSaveCallback(this::refreshData);

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

    private void deleteMovie(Movie movie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác Nhận Xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa phim này?");
        alert.setContentText(movie.getTitle());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                movieList.remove(movie);
                filteredList.remove(movie);
                updateSummary();
                showSuccess("Đã xóa phim thành công!");
            }
        });
    }

    private void refreshData() {
        // Reload data from service
        filteredList.setAll(movieList);
        updateSummary();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}