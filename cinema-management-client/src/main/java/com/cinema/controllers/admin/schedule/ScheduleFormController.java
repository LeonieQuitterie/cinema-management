package com.cinema.controllers.admin.schedule;

import com.cinema.models.*;
import com.cinema.utils.admin.CinemaApi;
import com.cinema.utils.admin.MovieApi;
import com.cinema.utils.admin.ShowtimeApi;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduleFormController implements Initializable {

    // Step 1: Movie Selection
    @FXML private TextField movieSearchField;
    @FXML private ListView<String> movieSuggestionsList;
    @FXML private VBox movieInfoCard;
    @FXML private Label selectedMovieTitle;
    @FXML private Label selectedMovieGenres;
    @FXML private Label selectedMovieDuration;
    
    // Step 2: Cinema Selection
    @FXML private ListView<CheckBox> cinemaListView;
    @FXML private Label selectedCinemasCount;
    
    // Step 3: Screen Selection
    @FXML private ListView<CheckBox> screenListView;
    @FXML private Label selectedScreensCount;
    
    // Step 4: Format
    @FXML private ComboBox<String> formatCombo;
    
    // Step 5: Date & Time
    @FXML private VBox dateScheduleContainer;
    
    // Preview
    @FXML private VBox previewSection;
    @FXML private Label totalShowtimesPreview;
    @FXML private ListView<String> previewList;
    
    // Validation
    @FXML private Label validationMessage;

    // Services
    private ShowtimeApi showtimeService;
    private MovieApi movieService;
    private CinemaApi cinemaService;
    
    // Data
    private List<Movie> allMovies = new ArrayList<>();
    private List<Cinema> allCinemas = new ArrayList<>();
    private Movie selectedMovie;
    private List<Cinema> selectedCinemas = new ArrayList<>();
    private List<ScreenWrapper> selectedScreens = new ArrayList<>();
    private List<DateSchedule> dateSchedules = new ArrayList<>();
    
    // Callback
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showtimeService = new ShowtimeApi();
        movieService = new MovieApi();
        cinemaService = new CinemaApi();

        setupMovieSearch();
        setupCinemaSelection();
        setupFormatCombo();
        loadMoviesFromAPI();
        loadCinemasFromAPI();
        // loadDummyData();
    }

    private void setupMovieSearch() {
        // Hide suggestions initially
        movieSuggestionsList.setVisible(false);
        movieSuggestionsList.setManaged(false);
        
        // Listen to text changes
        movieSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                movieSuggestionsList.setVisible(false);
                movieSuggestionsList.setManaged(false);
                return;
            }
            
            // Filter movies
            String search = newVal.toLowerCase().trim();
            List<String> suggestions = allMovies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(search))
                .map(Movie::getTitle)
                .collect(Collectors.toList());
            
            if (suggestions.isEmpty()) {
                movieSuggestionsList.setVisible(false);
                movieSuggestionsList.setManaged(false);
            } else {
                movieSuggestionsList.getItems().setAll(suggestions);
                movieSuggestionsList.setVisible(true);
                movieSuggestionsList.setManaged(true);
            }
        });
        
        // Handle selection
        movieSuggestionsList.setOnMouseClicked(event -> {
            String selectedTitle = movieSuggestionsList.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                selectMovie(selectedTitle);
            }
        });
    }

    private void selectMovie(String title) {
        selectedMovie = allMovies.stream()
            .filter(m -> m.getTitle().equals(title))
            .findFirst()
            .orElse(null);
        
        if (selectedMovie != null) {
            movieSearchField.setText(selectedMovie.getTitle());
            movieSuggestionsList.setVisible(false);
            movieSuggestionsList.setManaged(false);
            
            // Show movie info card
            selectedMovieTitle.setText(selectedMovie.getTitle());
            selectedMovieGenres.setText(
                selectedMovie.getGenres() != null && !selectedMovie.getGenres().isEmpty()
                    ? selectedMovie.getGenres().stream()
                        .map(g -> g.getName())
                        .collect(Collectors.joining(", "))
                    : "N/A"
            );
            selectedMovieDuration.setText(selectedMovie.getDuration() + " phút");
            
            movieInfoCard.setVisible(true);
            movieInfoCard.setManaged(true);
        }
    }

    @FXML
    private void clearMovieSelection() {
        selectedMovie = null;
        movieSearchField.clear();
        movieInfoCard.setVisible(false);
        movieInfoCard.setManaged(false);
    }

    private void setupCinemaSelection() {
        cinemaListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Update screen list when cinema selection changes
        cinemaListView.setOnMouseClicked(event -> {
            updateSelectedCinemas();
            updateScreenList();
        });
    }

    private void updateSelectedCinemas() {
        selectedCinemas.clear();
        int count = 0;
        
        for (CheckBox cb : cinemaListView.getItems()) {
            if (cb.isSelected()) {
                Cinema cinema = (Cinema) cb.getUserData();
                selectedCinemas.add(cinema);
                count++;
            }
        }
        
        selectedCinemasCount.setText(String.valueOf(count));
    }

    private void updateScreenList() {
        screenListView.getItems().clear();
        selectedScreens.clear();
        
        for (Cinema cinema : selectedCinemas) {
            if (cinema.getScreens() != null) {
                for (Screen screen : cinema.getScreens()) {
                    CheckBox cb = new CheckBox(cinema.getName() + " - " + screen.getName());
                    cb.setUserData(new ScreenWrapper(cinema, screen));
                    cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        updateSelectedScreens();
                    });
                    screenListView.getItems().add(cb);
                }
            }
        }
    }

    private void updateSelectedScreens() {
        selectedScreens.clear();
        int count = 0;
        
        for (CheckBox cb : screenListView.getItems()) {
            if (cb.isSelected()) {
                selectedScreens.add((ScreenWrapper) cb.getUserData());
                count++;
            }
        }
        
        selectedScreensCount.setText(String.valueOf(count));
    }

    private void setupFormatCombo() {
        formatCombo.getItems().addAll(
            "2D Phụ đề",
            "2D Lồng tiếng",
            "3D Phụ đề",
            "3D Lồng tiếng",
            "IMAX 2D",
            "IMAX 3D"
        );
    }

    private void loadMoviesFromAPI() {
        movieService.getAllMovies()
            .thenAccept(movies -> {
                Platform.runLater(() -> {
                    allMovies = movies;
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Không thể tải danh sách phim: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    private void loadCinemasFromAPI() {
        System.out.println("🎬 ScheduleFormController: Loading cinemas from API...");
        cinemaService.getAllCinemas()
            .thenAccept(cinemas -> {
                System.out.println("✅ ScheduleFormController: Received " + cinemas.size() + " cinemas");
                Platform.runLater(() -> {
                    allCinemas = cinemas;

                    System.out.println("🔄 ScheduleFormController: Adding cinemas to list view...");
                    
                    for (Cinema cinema : cinemas) {
                        System.out.println("  - Cinema: " + cinema.getName() + " (ID: " + cinema.getId() + ")");
                        System.out.println("    Screens: " + (cinema.getScreens() != null ? cinema.getScreens().size() : 0));
                        
                        CheckBox cb = new CheckBox(cinema.getName());
                        cb.setUserData(cinema);
                        cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                            updateSelectedCinemas();
                            updateScreenList();
                        });
                        cinemaListView.getItems().add(cb);
                    }

                    System.out.println("✅ ScheduleFormController: Cinema list view updated with " + cinemaListView.getItems().size() + " items");
                });
            })
            .exceptionally(ex -> {
                System.err.println("❌ ScheduleFormController: Error loading cinemas");
                ex.printStackTrace();
                Platform.runLater(() -> {
                    showError("Không thể tải danh sách rạp: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    @FXML
    private void addDateSchedule() {
        DateSchedule dateSchedule = new DateSchedule();
        dateSchedules.add(dateSchedule);
        
        VBox dateBox = createDateScheduleBox(dateSchedule);
        dateScheduleContainer.getChildren().add(dateBox);
    }

    private VBox createDateScheduleBox(DateSchedule dateSchedule) {
        VBox container = new VBox(12);
        container.getStyleClass().add("date-schedule-item");
        
        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label dateLabel = new Label("Ngày chiếu");
        dateLabel.getStyleClass().add("date-label");
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker-field");
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateSchedule.setDate(newVal);
        });
        dateSchedule.setDate(datePicker.getValue());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button removeBtn = new Button("✕ Xóa ngày này");
        removeBtn.getStyleClass().add("btn-remove-date");
        removeBtn.setOnAction(e -> {
            dateSchedules.remove(dateSchedule);
            dateScheduleContainer.getChildren().remove(container);
        });
        
        header.getChildren().addAll(dateLabel, datePicker, spacer, removeBtn);
        
        // Time slots container
        VBox timeSlotsBox = new VBox(8);
        dateSchedule.setTimeSlotsBox(timeSlotsBox);
        
        // Add time button
        Button addTimeBtn = new Button("+ Thêm giờ chiếu");
        addTimeBtn.getStyleClass().add("btn-add-time");
        addTimeBtn.setOnAction(e -> addTimeSlot(dateSchedule, timeSlotsBox));
        
        container.getChildren().addAll(header, timeSlotsBox, addTimeBtn);
        
        // Add first time slot
        addTimeSlot(dateSchedule, timeSlotsBox);
        
        return container;
    }

    private void addTimeSlot(DateSchedule dateSchedule, VBox timeSlotsBox) {
        HBox timeBox = new HBox(10);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("Giờ chiếu:");
        label.getStyleClass().add("schedule-field-label");
        
        TextField timeField = new TextField();
        timeField.getStyleClass().add("time-slot-field");
        timeField.setPromptText("HH:mm (VD: 14:30)");
        timeField.setPrefWidth(120);
        
        Label endTimeLabel = new Label();
        endTimeLabel.getStyleClass().add("end-time-label");
        
        // Calculate end time when start time changes
        timeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (selectedMovie != null && newVal != null && !newVal.isEmpty()) {
                try {
                    LocalTime startTime = LocalTime.parse(newVal, DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime endTime = startTime.plusMinutes(selectedMovie.getDuration());
                    endTimeLabel.setText("→ Kết thúc: " + endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                } catch (Exception e) {
                    endTimeLabel.setText("");
                }
            }
        });
        
        Button removeTimeBtn = new Button("✕");
        removeTimeBtn.getStyleClass().add("btn-remove-time");
        removeTimeBtn.setOnAction(e -> {
            dateSchedule.getTimeSlots().remove(timeField);
            timeSlotsBox.getChildren().remove(timeBox);
        });
        
        timeBox.getChildren().addAll(label, timeField, endTimeLabel, removeTimeBtn);
        timeSlotsBox.getChildren().add(timeBox);
        
        dateSchedule.getTimeSlots().add(timeField);
    }

    @FXML
    private void showPreview() {
        if (!validateForm()) {
            return;
        }
        
        List<String> previewItems = generatePreview();
        previewList.getItems().setAll(previewItems);
        totalShowtimesPreview.setText(String.valueOf(previewItems.size()));
        
        previewSection.setVisible(true);
        previewSection.setManaged(true);
    }

    @FXML
    private void closePreview() {
        previewSection.setVisible(false);
        previewSection.setManaged(false);
    }

    private List<String> generatePreview() {
        List<String> items = new ArrayList<>();
        String format = formatCombo.getValue();
        
        for (DateSchedule ds : dateSchedules) {
            LocalDate date = ds.getDate();
            for (TextField tf : ds.getTimeSlots()) {
                String timeStr = tf.getText().trim();
                if (!timeStr.isEmpty()) {
                    try {
                        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                        
                        for (ScreenWrapper sw : selectedScreens) {
                            String preview = String.format(
                                "%s | %s - %s | %s | %s",
                                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                sw.getCinema().getName(),
                                sw.getScreen().getName(),
                                time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                format
                            );
                            items.add(preview);
                        }
                    } catch (Exception e) {
                        // Skip invalid time
                    }
                }
            }
        }
        
        return items;
    }

   @FXML
    private void saveSchedule() {
        if (!validateForm()) {
            return;
        }
        
        // Generate showtimes
        List<Showtime> showtimes = new ArrayList<>();
        String format = formatCombo.getValue();
        double basePrice = 80000; // Default price
        
        for (DateSchedule ds : dateSchedules) {
            LocalDate date = ds.getDate();
            for (TextField tf : ds.getTimeSlots()) {
                String timeStr = tf.getText().trim();
                if (!timeStr.isEmpty()) {
                    try {
                        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                        LocalDateTime startTime = LocalDateTime.of(date, time);
                        LocalDateTime endTime = startTime.plusMinutes(selectedMovie.getDuration());
                        
                        for (ScreenWrapper sw : selectedScreens) {
                            Showtime st = new Showtime();
                            st.setMovieId(selectedMovie.getId());
                            st.setScreenId(sw.getScreen().getId());
                            st.setStartTime(startTime);
                            st.setEndTime(endTime);
                            st.setBasePrice(basePrice);
                            st.setFormat(format);
                            
                            showtimes.add(st);
                        }
                    } catch (Exception e) {
                        // Skip invalid time
                    }
                }
            }
        }
        
        // Save to API
        showtimeService.createBulkShowtimes(showtimes)
            .thenAccept(count -> {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành Công");
                    alert.setHeaderText("Đã tạo lịch chiếu thành công!");
                    alert.setContentText("Đã tạo " + count + " suất chiếu.");
                    alert.showAndWait();
                    
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    
                    cancel();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Không thể tạo lịch chiếu: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) movieSearchField.getScene().getWindow();
        stage.close();
    }

    private boolean validateForm() {
        validationMessage.setVisible(false);
        validationMessage.setManaged(false);
        
        if (selectedMovie == null) {
            showValidation("Vui lòng chọn phim");
            return false;
        }
        
        if (selectedCinemas.isEmpty()) {
            showValidation("Vui lòng chọn ít nhất 1 rạp");
            return false;
        }
        
        if (selectedScreens.isEmpty()) {
            showValidation("Vui lòng chọn ít nhất 1 phòng chiếu");
            return false;
        }
        
        if (formatCombo.getValue() == null || formatCombo.getValue().isEmpty()) {
            showValidation("Vui lòng chọn định dạng chiếu");
            return false;
        }
        
        if (dateSchedules.isEmpty()) {
            showValidation("Vui lòng thêm ít nhất 1 ngày chiếu");
            return false;
        }
        
        // Validate time slots
        for (DateSchedule ds : dateSchedules) {
            if (ds.getTimeSlots().isEmpty()) {
                showValidation("Mỗi ngày chiếu phải có ít nhất 1 giờ chiếu");
                return false;
            }
            
            for (TextField tf : ds.getTimeSlots()) {
                String time = tf.getText().trim();
                if (time.isEmpty()) {
                    showValidation("Vui lòng nhập giờ chiếu");
                    return false;
                }
                
                try {
                    LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception e) {
                    showValidation("Giờ chiếu không hợp lệ: " + time);
                    return false;
                }
            }
        }
        
        return true;
    }

    private void showValidation(String message) {
        validationMessage.setText("⚠ " + message);
        validationMessage.setVisible(true);
        validationMessage.setManaged(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    // private void loadDummyData() {
    //     // Load movies
    //     Movie movie1 = new Movie();
    //     movie1.setId("M1");
    //     movie1.setTitle("Avengers: Endgame");
    //     movie1.setDuration(181);
    //     // movie1.setGenres(Arrays.asList("Hành động", "Khoa học viễn tưởng"));
    //     allMovies.add(movie1);
        
    //     Movie movie2 = new Movie();
    //     movie2.setId("M2");
    //     movie2.setTitle("Spider-Man: No Way Home");
    //     movie2.setDuration(148);
    //     // movie2.setGenres(Arrays.asList("Hành động", "Phiêu lưu"));
    //     allMovies.add(movie2);
        
    //     Movie movie3 = new Movie();
    //     movie3.setId("M3");
    //     movie3.setTitle("Deadpool & Wolverine");
    //     movie3.setDuration(128);
    //     // movie3.setGenres(Arrays.asList("Hành động", "Hài"));
    //     allMovies.add(movie3);
        
    //     // Load cinemas
    //     Cinema cinema1 = new Cinema();
    //     cinema1.setId("C1");
    //     cinema1.setName("CGV Vincom");
    //     cinema1.setScreens(new ArrayList<>());
        
    //     Screen s1 = new Screen();
    //     s1.setId("S1");
    //     s1.setName("Phòng 1");
    //     s1.setTotalSeats(100);
    //     cinema1.getScreens().add(s1);
        
    //     Screen s2 = new Screen();
    //     s2.setId("S2");
    //     s2.setName("Phòng 2");
    //     s2.setTotalSeats(80);
    //     cinema1.getScreens().add(s2);
        
    //     allCinemas.add(cinema1);
        
    //     Cinema cinema2 = new Cinema();
    //     cinema2.setId("C2");
    //     cinema2.setName("Lotte Cinema");
    //     cinema2.setScreens(new ArrayList<>());
        
    //     Screen s3 = new Screen();
    //     s3.setId("S3");
    //     s3.setName("Phòng IMAX");
    //     s3.setTotalSeats(150);
    //     cinema2.getScreens().add(s3);
        
    //     Screen s4 = new Screen();
    //     s4.setId("S4");
    //     s4.setName("Phòng VIP");
    //     s4.setTotalSeats(60);
    //     cinema2.getScreens().add(s4);
        
    //     allCinemas.add(cinema2);
        
    //     // Populate cinema list
    //     for (Cinema cinema : allCinemas) {
    //         CheckBox cb = new CheckBox(cinema.getName());
    //         cb.setUserData(cinema);
    //         cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
    //             updateSelectedCinemas();
    //             updateScreenList();
    //         });
    //         cinemaListView.getItems().add(cb);
    //     }
    // }

    // Helper classes
    private static class DateSchedule {
        private LocalDate date;
        private List<TextField> timeSlots = new ArrayList<>();
        private VBox timeSlotsBox;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public List<TextField> getTimeSlots() { return timeSlots; }
        public VBox getTimeSlotsBox() { return timeSlotsBox; }
        public void setTimeSlotsBox(VBox box) { this.timeSlotsBox = box; }
    }

    private static class ScreenWrapper {
        private Cinema cinema;
        private Screen screen;

        public ScreenWrapper(Cinema cinema, Screen screen) {
            this.cinema = cinema;
            this.screen = screen;
        }

        public Cinema getCinema() { return cinema; }
        public Screen getScreen() { return screen; }
    }
}