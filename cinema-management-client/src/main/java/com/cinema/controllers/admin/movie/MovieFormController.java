package com.cinema.controllers.admin.movie;

import com.cinema.models.Actor;
import com.cinema.models.Genre;
import com.cinema.models.Movie;
import com.cinema.models.Movie.MovieStatus;
import com.cinema.utils.admin.MovieApi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MovieFormController implements Initializable {

    @FXML private Label formTitleLabel;
    
    // Basic Information
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private TextField durationField;
    @FXML private DatePicker releaseDatePicker;

    // Classification
    @FXML private ComboBox<String> statusBox;
    @FXML private ComboBox<String> ageBox;
    @FXML private TextField languageField;
    @FXML private TextField ageDescField;
    
    // Genres
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ListView<String> genreListView;
    
    // Media
    @FXML private TextField posterField;
    @FXML private TextField trailerField;
    
    // Actors (Optional)
    @FXML private TextField actorNameField;
    @FXML private TextField actorRoleField;
    @FXML private ListView<String> actorListView;
    
    // Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Movie currentMovie;
    private Consumer<Movie> onSaveCallback;
    private MovieApi movieService;
    
    private ObservableList<String> selectedGenreNames = FXCollections.observableArrayList();
    private ObservableList<String> actorDisplayList = FXCollections.observableArrayList();
    
    private List<Genre> availableGenres = new ArrayList<>();
    private List<Genre> selectedGenres = new ArrayList<>();
    private List<Actor> actors = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        movieService = new MovieApi();
        
        setupComboBoxes();
        setupListViews();
        loadGenresFromAPI();
    }

    private void setupComboBoxes() {
        // Status options
        statusBox.getItems().addAll("Đang chiếu", "Sắp chiếu");
        
        // Age rating options
        ageBox.getItems().addAll("P", "C13", "C16", "C18");
    }

    private void setupListViews() {
        genreListView.setItems(selectedGenreNames);
        actorListView.setItems(actorDisplayList);
        
        // Allow removing genres on double-click
        genreListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                removeSelectedGenre();
            }
        });
        
        // Allow removing actors on double-click
        actorListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                removeSelectedActor();
            }
        });
    }

    /**
     * Load genres from API
     */
    private void loadGenresFromAPI() {
        movieService.getAllGenres()
            .thenAccept(genres -> {
                Platform.runLater(() -> {
                    availableGenres = genres;
                    genreComboBox.getItems().clear();
                    genreComboBox.getItems().addAll(
                        genres.stream()
                            .map(Genre::getName)
                            .collect(Collectors.toList())
                    );
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    // Fallback to hardcoded genres
                    genreComboBox.getItems().addAll(
                        "Hành động", "Kinh dị", "Hài", "Tình cảm", "Khoa học viễn tưởng"
                    );
                });
                ex.printStackTrace();
                return null;
            });
    }

    /**
     * Set movie for editing (null for new movie)
     */
    public void setMovie(Movie movie) {
        this.currentMovie = movie;
        
        if (movie == null) {
            formTitleLabel.setText("THÊM PHIM MỚI");
            // Set default values
            statusBox.setValue("Sắp chiếu");
            releaseDatePicker.setValue(LocalDate.now());
        } else {
            formTitleLabel.setText("CHỈNH SỬA PHIM");
            fillFormData();
        }
    }

    /**
     * Fill form with existing movie data
     */
    private void fillFormData() {
        titleField.setText(currentMovie.getTitle());
        descField.setText(currentMovie.getDescription());
        durationField.setText(String.valueOf(currentMovie.getDuration()));
        languageField.setText(currentMovie.getLanguage());
        posterField.setText(currentMovie.getPosterUrl());
        trailerField.setText(currentMovie.getTrailerUrl());
        ageDescField.setText(currentMovie.getAgeRatingDescription());
        
        // Release Date
        if (currentMovie.getReleaseDate() != null) {
            releaseDatePicker.setValue(currentMovie.getReleaseDate());
        }
        
        // Status
        if (currentMovie.getStatus() != null) {
            switch (currentMovie.getStatus()) {
                case NOW_SHOWING: statusBox.setValue("Đang chiếu"); break;
                case COMING_SOON: statusBox.setValue("Sắp chiếu"); break;
            }
        }
        
        // Age Rating
        ageBox.setValue(currentMovie.getAgeRating());
        
        // Genres
        if (currentMovie.getGenres() != null && !currentMovie.getGenres().isEmpty()) {
            selectedGenres.addAll(currentMovie.getGenres());
            selectedGenreNames.addAll(
                currentMovie.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList())
            );
        }
        
        // Actors
        if (currentMovie.getActors() != null && !currentMovie.getActors().isEmpty()) {
            actors.addAll(currentMovie.getActors());
            for (Actor actor : currentMovie.getActors()) {
                actorDisplayList.add(actor.getRealName() + " - " + actor.getCharacter());
            }
        }
    }

    /**
     * Add selected genre
     */
    @FXML
    private void addGenre() {
        String selectedGenreName = genreComboBox.getValue();
        
        if (selectedGenreName != null && !selectedGenreNames.contains(selectedGenreName)) {
            // Find the Genre object
            Genre genre = availableGenres.stream()
                .filter(g -> g.getName().equals(selectedGenreName))
                .findFirst()
                .orElse(null);
            
            if (genre != null) {
                selectedGenres.add(genre);
                selectedGenreNames.add(selectedGenreName);
            } else {
                // If genre not found in available genres, create temporary one
                Genre tempGenre = new Genre();
                tempGenre.setName(selectedGenreName);
                selectedGenres.add(tempGenre);
                selectedGenreNames.add(selectedGenreName);
            }
            
            genreComboBox.setValue(null);
        }
    }

    /**
     * Remove selected genre
     */
    @FXML
    private void removeSelectedGenre() {
        String selected = genreListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int index = selectedGenreNames.indexOf(selected);
            if (index >= 0) {
                selectedGenreNames.remove(index);
                selectedGenres.remove(index);
            }
        }
    }

    /**
     * Add actor
     */
    @FXML
    private void addActor() {
        String name = actorNameField.getText().trim();
        String role = actorRoleField.getText().trim();
        
        if (!name.isEmpty() && !role.isEmpty()) {
            Actor actor = new Actor();
            actor.setRealName(name);
            actor.setCharacter(role);
            actors.add(actor);
            
            actorDisplayList.add(name + " - " + role);
            
            actorNameField.clear();
            actorRoleField.clear();
        }
    }

    /**
     * Remove selected actor
     */
    @FXML
    private void removeSelectedActor() {
        int selectedIndex = actorListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            actorDisplayList.remove(selectedIndex);
            actors.remove(selectedIndex);
        }
    }

    /**
     * Save movie
     */
    @FXML
    private void save() {
        if (!validateForm()) {
            return;
        }

        // Disable save button to prevent double-click
        saveButton.setDisable(true);

        // Create or update movie object
        if (currentMovie == null) {
            currentMovie = new Movie();
        }

        // Basic info
        currentMovie.setTitle(titleField.getText().trim());
        currentMovie.setDescription(descField.getText().trim());
        currentMovie.setDuration(Integer.parseInt(durationField.getText().trim()));
        currentMovie.setLanguage(languageField.getText().trim());
        currentMovie.setAgeRating(ageBox.getValue());
        currentMovie.setAgeRatingDescription(ageDescField.getText().trim());
        
        // Release Date
        currentMovie.setReleaseDate(releaseDatePicker.getValue());
        
        // Status
        String status = statusBox.getValue();
        if (status != null) {
            switch (status) {
                case "Đang chiếu": 
                    currentMovie.setStatus(MovieStatus.NOW_SHOWING); 
                    break;
                case "Sắp chiếu": 
                    currentMovie.setStatus(MovieStatus.COMING_SOON); 
                    break;
            }
        }
        
        // Media
        currentMovie.setPosterUrl(posterField.getText().trim());
        currentMovie.setTrailerUrl(trailerField.getText().trim());
        
        // Genres
        currentMovie.setGenres(new ArrayList<>(selectedGenres));
        
        // Actors (optional)
        currentMovie.setActors(actors);
        
        // Trigger callback
        if (onSaveCallback != null) {
            onSaveCallback.accept(currentMovie);
        }
        
        close();
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        // Title
        if (titleField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập tên phim!");
            titleField.requestFocus();
            return false;
        }
        
        // Description
        if (descField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập mô tả phim!");
            descField.requestFocus();
            return false;
        }
        
        // Duration
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0 || duration > 500) {
                showError("Thời lượng phải từ 1-500 phút!");
                durationField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Thời lượng phải là số!");
            durationField.requestFocus();
            return false;
        }
        
        // Release Date
        if (releaseDatePicker.getValue() == null) {
            showError("Vui lòng chọn ngày phát hành!");
            releaseDatePicker.requestFocus();
            return false;
        }
        
        // Status
        if (statusBox.getValue() == null) {
            showError("Vui lòng chọn trạng thái!");
            statusBox.requestFocus();
            return false;
        }
        
        // Age Rating
        if (ageBox.getValue() == null) {
            showError("Vui lòng chọn độ tuổi!");
            ageBox.requestFocus();
            return false;
        }
        
        // Language
        if (languageField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập ngôn ngữ!");
            languageField.requestFocus();
            return false;
        }
        
        // Poster URL
        if (posterField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập URL poster!");
            posterField.requestFocus();
            return false;
        }
        
        // Genres (at least one)
        if (selectedGenres.isEmpty()) {
            showError("Vui lòng chọn ít nhất 1 thể loại!");
            genreComboBox.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Close dialog
     */
    @FXML
    private void close() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    /**
     * Set callback for save action
     */
    public void setOnSaveCallback(Consumer<Movie> callback) {
        this.onSaveCallback = callback;
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show success alert
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}