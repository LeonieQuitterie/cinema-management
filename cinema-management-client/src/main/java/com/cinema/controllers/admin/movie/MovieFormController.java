package com.cinema.controllers.admin.movie;

import com.cinema.models.Actor;
import com.cinema.models.Movie;
import com.cinema.models.Movie.MovieStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MovieFormController implements Initializable {

    @FXML private Label formTitleLabel;
    
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private TextField durationField;

    @FXML private ComboBox<String> statusBox;
    @FXML private ComboBox<String> ageBox;
    @FXML private TextField languageField;
    
    @FXML private TextField genreInputField;
    @FXML private ListView<String> genreListView;
    
    @FXML private TextField posterField;
    @FXML private TextField trailerField;
    
    @FXML private TextField actorNameField;
    @FXML private TextField actorRoleField;
    @FXML private ListView<String> actorListView;
    
    @FXML private TextField ageDescField;

    private Movie currentMovie;
    private Runnable onSaveCallback;
    
    private ObservableList<String> genres = FXCollections.observableArrayList();
    private ObservableList<String> actorDisplayList = FXCollections.observableArrayList();
    private ArrayList<Actor> actors = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
        genreListView.setItems(genres);
        actorListView.setItems(actorDisplayList);
    }

    private void setupComboBoxes() {
        statusBox.getItems().addAll("Đang chiếu", "Sắp chiếu", "Đã kết thúc");
        ageBox.getItems().addAll("P", "C13", "C16", "C18");
    }

    public void setMovie(Movie movie) {
        this.currentMovie = movie;
        
        if (movie == null) {
            formTitleLabel.setText("THÊM PHIM MỚI");
        } else {
            formTitleLabel.setText("CHỈNH SỬA PHIM");
            fillFormData();
        }
    }

    private void fillFormData() {
        titleField.setText(currentMovie.getTitle());
        descField.setText(currentMovie.getDescription());
        durationField.setText(String.valueOf(currentMovie.getDuration()));
        languageField.setText(currentMovie.getLanguage());
        posterField.setText(currentMovie.getPosterUrl());
        trailerField.setText(currentMovie.getTrailerUrl());
        ageDescField.setText(currentMovie.getAgeRatingDescription());
        
        // Status
        if (currentMovie.getStatus() != null) {
            switch (currentMovie.getStatus()) {
                case NOW_SHOWING: statusBox.setValue("Đang chiếu"); break;
                case COMING_SOON: statusBox.setValue("Sắp chiếu"); break;
            }
        }
        
        ageBox.setValue(currentMovie.getAgeRating());
        
        // Genres
        if (currentMovie.getGenres() != null) {
            genres.addAll(currentMovie.getGenres());
        }
        
        // Actors
        if (currentMovie.getActors() != null) {
            actors.addAll(currentMovie.getActors());
            for (Actor actor : currentMovie.getActors()) {
                actorDisplayList.add(actor.getRealName() + " - " + actor.getCharacter());
            }
        }
    }

    @FXML
    private void addGenre() {
        String genre = genreInputField.getText().trim();
        if (!genre.isEmpty() && !genres.contains(genre)) {
            genres.add(genre);
            genreInputField.clear();
        }
    }

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

    @FXML
    private void save() {
        if (!validateForm()) {
            return;
        }

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
        
        // Status
        String status = statusBox.getValue();
        if (status != null) {
            switch (status) {
                case "Đang chiếu": currentMovie.setStatus(MovieStatus.NOW_SHOWING); break;
                case "Sắp chiếu": currentMovie.setStatus(MovieStatus.COMING_SOON); break;
            }
        }
        
        // Media
        currentMovie.setPosterUrl(posterField.getText().trim());
        currentMovie.setTrailerUrl(trailerField.getText().trim());
        
        // Genres & Actors
        currentMovie.setGenres(new ArrayList<>(genres));
        currentMovie.setActors(actors);
        
        // TODO: Save to database via service
        
        showSuccess("Đã lưu phim thành công!");
        
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        close();
    }

    private boolean validateForm() {
        if (titleField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập tên phim!");
            return false;
        }
        
        if (descField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập mô tả phim!");
            return false;
        }
        
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0) {
                showError("Thời lượng phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Thời lượng phải là số!");
            return false;
        }
        
        
        if (statusBox.getValue() == null) {
            showError("Vui lòng chọn trạng thái!");
            return false;
        }
        
        if (ageBox.getValue() == null) {
            showError("Vui lòng chọn độ tuổi!");
            return false;
        }
        
        if (languageField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập ngôn ngữ!");
            return false;
        }
        
        if (posterField.getText().trim().isEmpty()) {
            showError("Vui lòng nhập URL poster!");
            return false;
        }
        
        return true;
    }

    @FXML
    private void close() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
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