package com.cinema.controllers.admin.schedule;

import com.cinema.models.*;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ScheduleCalendarController implements Initializable {

    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> cinemaFilter;
    @FXML
    private ComboBox<String> screenFilter;
    @FXML
    private ComboBox<String> movieFilter;
    @FXML
    private TextField searchField;

    @FXML
    private Label totalShowtimesLabel;
    @FXML
    private Label ongoingShowtimesLabel;
    @FXML
    private Label upcomingShowtimesLabel;
    @FXML
    private Label completedShowtimesLabel;

    @FXML
    private TableView<ShowtimeDisplay> showtimeTable;
    @FXML
    private TableColumn<ShowtimeDisplay, String> dateCol;
    @FXML
    private TableColumn<ShowtimeDisplay, String> timeCol;
    @FXML
    private TableColumn<ShowtimeDisplay, String> movieCol;
    @FXML
    private TableColumn<ShowtimeDisplay, String> cinemaCol;
    @FXML
    private TableColumn<ShowtimeDisplay, String> screenCol;
    @FXML
    private TableColumn<ShowtimeDisplay, String> durationCol;
    @FXML
    private TableColumn<ShowtimeDisplay, String> seatsCol;
    @FXML
    private TableColumn<ShowtimeDisplay, String> statusCol;
    @FXML
    private TableColumn<ShowtimeDisplay, Void> actionCol;

    private ObservableList<ShowtimeDisplay> showtimeList = FXCollections.observableArrayList();
    private ObservableList<ShowtimeDisplay> filteredList = FXCollections.observableArrayList();

    // Dummy data storage
    private List<Movie> movies = new ArrayList<>();
    private List<Cinema> cinemas = new ArrayList<>();
    private List<Showtime> showtimes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();
        loadDummyData();

        // Set default date to today
        datePicker.setValue(LocalDate.now());

        applyFilters();
        updateSummary();
    }

    private void setupTable() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateStr"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeStr"));
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));

        cinemaCol.setCellValueFactory(new PropertyValueFactory<>("cinemaName"));
        screenCol.setCellValueFactory(new PropertyValueFactory<>("screenName"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationStr"));
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seatsStr"));

        // Status column with colored label
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    if (status.equals("Đang chiếu")) {
                        label.getStyleClass().add("status-ongoing");
                    } else if (status.equals("Sắp chiếu")) {
                        label.getStyleClass().add("status-upcoming");
                    } else {
                        label.getStyleClass().add("status-completed");
                    }
                    setGraphic(label);
                }
            }
        });

        // Action column with buttons
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Xem");
            private final Button editBtn = new Button("Sửa");
            private final Button delBtn = new Button("Xóa");
            private final HBox box = new HBox(6, viewBtn, editBtn, delBtn);

            {
                box.setAlignment(Pos.CENTER);
                viewBtn.getStyleClass().add("btn-view");
                editBtn.getStyleClass().add("btn-edit");
                delBtn.getStyleClass().add("btn-delete");

                viewBtn.setOnAction(e -> {
                    ShowtimeDisplay display = getTableView().getItems().get(getIndex());
                    viewShowtime(display);
                });

                editBtn.setOnAction(e -> {
                    ShowtimeDisplay display = getTableView().getItems().get(getIndex());
                    editShowtime(display);
                });

                delBtn.setOnAction(e -> {
                    ShowtimeDisplay display = getTableView().getItems().get(getIndex());
                    deleteShowtime(display);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        showtimeTable.setItems(filteredList);
    }

    private void setupFilters() {
        cinemaFilter.getItems().add("Tất cả rạp");
        screenFilter.getItems().add("Tất cả phòng");
        movieFilter.getItems().add("Tất cả phim");

        cinemaFilter.setValue("Tất cả rạp");
        screenFilter.setValue("Tất cả phòng");
        movieFilter.setValue("Tất cả phim");

        // Add listener to cinema filter to update screen filter
        cinemaFilter.setOnAction(e -> updateScreenFilter());
    }

    private void loadDummyData() {
        // Load dummy movies
        Movie movie1 = new Movie();
        movie1.setId("M1");
        movie1.setTitle("Avengers: Endgame");
        movie1.setDuration(181);
        movies.add(movie1);

        Movie movie2 = new Movie();
        movie2.setId("M2");
        movie2.setTitle("Spider-Man: No Way Home");
        movie2.setDuration(148);
        movies.add(movie2);

        Movie movie3 = new Movie();
        movie3.setId("M3");
        movie3.setTitle("Deadpool & Wolverine");
        movie3.setDuration(128);
        movies.add(movie3);

        // Load dummy cinemas with screens
        Cinema cinema1 = new Cinema();
        cinema1.setId("C1");
        cinema1.setName("CGV Vincom");
        cinema1.setScreens(new ArrayList<>());

        Screen screen1 = new Screen();
        screen1.setId("S1");
        screen1.setName("Phòng 1");
        screen1.setCinemaId("C1");
        screen1.setTotalSeats(100);
        cinema1.getScreens().add(screen1);

        Screen screen2 = new Screen();
        screen2.setId("S2");
        screen2.setName("Phòng 2");
        screen2.setCinemaId("C1");
        screen2.setTotalSeats(80);
        cinema1.getScreens().add(screen2);

        cinemas.add(cinema1);

        Cinema cinema2 = new Cinema();
        cinema2.setId("C2");
        cinema2.setName("Lotte Cinema");
        cinema2.setScreens(new ArrayList<>());

        Screen screen3 = new Screen();
        screen3.setId("S3");
        screen3.setName("Phòng IMAX");
        screen3.setCinemaId("C2");
        screen3.setTotalSeats(150);
        cinema2.getScreens().add(screen3);

        cinemas.add(cinema2);

        // Load dummy showtimes
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Today's showtimes
        Showtime st1 = new Showtime();
        st1.setId("ST1");
        st1.setMovieId("M1");
        st1.setScreenId("S1");
        st1.setStartTime(today.withHour(14).withMinute(0));
        st1.setEndTime(today.withHour(14).withMinute(0).plusMinutes(181));
        st1.setBasePrice(80000);
        st1.setBookedSeats(new ArrayList<>());
        for (int i = 1; i <= 30; i++) {
            st1.getBookedSeats().add("A" + i);
        }
        showtimes.add(st1);

        Showtime st2 = new Showtime();
        st2.setId("ST2");
        st2.setMovieId("M2");
        st2.setScreenId("S2");
        st2.setStartTime(today.withHour(16).withMinute(30));
        st2.setEndTime(today.withHour(16).withMinute(30).plusMinutes(148));
        st2.setBasePrice(75000);
        st2.setBookedSeats(new ArrayList<>());
        for (int i = 1; i <= 20; i++) {
            st2.getBookedSeats().add("B" + i);
        }
        showtimes.add(st2);

        Showtime st3 = new Showtime();
        st3.setId("ST3");
        st3.setMovieId("M3");
        st3.setScreenId("S3");
        st3.setStartTime(today.withHour(19).withMinute(0));
        st3.setEndTime(today.withHour(19).withMinute(0).plusMinutes(128));
        st3.setBasePrice(90000);
        st3.setBookedSeats(new ArrayList<>());
        showtimes.add(st3);

        // Tomorrow's showtimes
        Showtime st4 = new Showtime();
        st4.setId("ST4");
        st4.setMovieId("M1");
        st4.setScreenId("S1");
        st4.setStartTime(today.plusDays(1).withHour(15).withMinute(0));
        st4.setEndTime(today.plusDays(1).withHour(15).withMinute(0).plusMinutes(181));
        st4.setBasePrice(80000);
        st4.setBookedSeats(new ArrayList<>());
        showtimes.add(st4);

        // Update filter dropdowns
        movieFilter.getItems().addAll(
                movies.stream().map(Movie::getTitle).collect(Collectors.toList()));

        cinemaFilter.getItems().addAll(
                cinemas.stream().map(Cinema::getName).collect(Collectors.toList()));
    }

    @FXML
    private void applyFilters() {
        filteredList.clear();

        LocalDate selectedDate = datePicker.getValue();
        String selectedCinema = cinemaFilter.getValue();
        String selectedScreen = screenFilter.getValue();
        String selectedMovie = movieFilter.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        for (Showtime showtime : showtimes) {
            // Date filter
            if (selectedDate != null && !showtime.getStartTime().toLocalDate().equals(selectedDate)) {
                continue;
            }

            // Find movie and cinema for this showtime
            Movie movie = movies.stream()
                    .filter(m -> m.getId().equals(showtime.getMovieId()))
                    .findFirst().orElse(null);

            if (movie == null)
                continue;

            // Search filter
            if (!searchText.isEmpty() && !movie.getTitle().toLowerCase().contains(searchText)) {
                continue;
            }

            // Movie filter
            if (!selectedMovie.equals("Tất cả phim") && !movie.getTitle().equals(selectedMovie)) {
                continue;
            }

            // Find screen and cinema
            Screen screen = null;
            Cinema cinema = null;
            for (Cinema c : cinemas) {
                for (Screen s : c.getScreens()) {
                    if (s.getId().equals(showtime.getScreenId())) {
                        screen = s;
                        cinema = c;
                        break;
                    }
                }
                if (screen != null)
                    break;
            }

            if (screen == null || cinema == null)
                continue;

            // Cinema filter
            if (!selectedCinema.equals("Tất cả rạp") && !cinema.getName().equals(selectedCinema)) {
                continue;
            }

            // Screen filter
            if (!selectedScreen.equals("Tất cả phòng") && !screen.getName().equals(selectedScreen)) {
                continue;
            }

            // Create display object
            ShowtimeDisplay display = new ShowtimeDisplay();
            display.setShowtime(showtime);
            display.setMovie(movie);
            display.setCinema(cinema);
            display.setScreen(screen);
            display.setDateStr(showtime.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            display.setTimeStr(showtime.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                    " - " + showtime.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            display.setMovieTitle(movie.getTitle());
            display.setCinemaName(cinema.getName());
            display.setScreenName(screen.getName());
            display.setDurationStr(movie.getDuration() + " phút");

            int totalSeats = screen.getTotalSeats();
            int bookedSeats = showtime.getBookedSeats() != null ? showtime.getBookedSeats().size() : 0;
            int availableSeats = totalSeats - bookedSeats;
            display.setSeatsStr(availableSeats + "/" + totalSeats);

            // Determine status
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(showtime.getEndTime())) {
                display.setStatus("Đã chiếu");
            } else if (now.isAfter(showtime.getStartTime())) {
                display.setStatus("Đang chiếu");
            } else {
                display.setStatus("Sắp chiếu");
            }

            filteredList.add(display);
        }

        updateSummary();
    }

    @FXML
    private void resetFilters() {
        datePicker.setValue(LocalDate.now());
        cinemaFilter.setValue("Tất cả rạp");
        screenFilter.setValue("Tất cả phòng");
        movieFilter.setValue("Tất cả phim");
        searchField.clear();
        applyFilters();
    }

    @FXML
    private void previousDay() {
        if (datePicker.getValue() != null) {
            datePicker.setValue(datePicker.getValue().minusDays(1));
            applyFilters();
        }
    }

    @FXML
    private void nextDay() {
        if (datePicker.getValue() != null) {
            datePicker.setValue(datePicker.getValue().plusDays(1));
            applyFilters();
        }
    }

    @FXML
    private void selectToday() {
        datePicker.setValue(LocalDate.now());
        applyFilters();
    }

    @FXML
    private void selectTomorrow() {
        datePicker.setValue(LocalDate.now().plusDays(1));
        applyFilters();
    }

    @FXML
    private void selectThisWeek() {
        // For simplicity, just select today
        // You can implement week view separately
        selectToday();
    }

    @FXML
    private void onDateChanged() {
        applyFilters();
    }

    private void updateScreenFilter() {
        String selectedCinema = cinemaFilter.getValue();
        screenFilter.getItems().clear();
        screenFilter.getItems().add("Tất cả phòng");

        if (!selectedCinema.equals("Tất cả rạp")) {
            Cinema cinema = cinemas.stream()
                    .filter(c -> c.getName().equals(selectedCinema))
                    .findFirst().orElse(null);

            if (cinema != null && cinema.getScreens() != null) {
                screenFilter.getItems().addAll(
                        cinema.getScreens().stream()
                                .map(Screen::getName)
                                .collect(Collectors.toList()));
            }
        }

        screenFilter.setValue("Tất cả phòng");
    }

    private void updateSummary() {
        int total = filteredList.size();
        long ongoing = filteredList.stream()
                .filter(s -> s.getStatus().equals("Đang chiếu"))
                .count();
        long upcoming = filteredList.stream()
                .filter(s -> s.getStatus().equals("Sắp chiếu"))
                .count();
        long completed = filteredList.stream()
                .filter(s -> s.getStatus().equals("Đã chiếu"))
                .count();

        totalShowtimesLabel.setText(String.valueOf(total));
        ongoingShowtimesLabel.setText(String.valueOf(ongoing));
        upcomingShowtimesLabel.setText(String.valueOf(upcoming));
        completedShowtimesLabel.setText(String.valueOf(completed));
    }

    @FXML
    private void openAddSchedule() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/admin/partials/schedule/schedule-form-content.fxml"));
            Parent root = loader.load();

            ScheduleFormController controller = loader.getController();
            controller.setOnSaveCallback(this::applyFilters);

            Stage stage = new Stage();
            stage.setTitle("Thêm Lịch Chiếu Mới");
            stage.setScene(new Scene(root, 900, 700));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(showtimeTable.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Không thể mở form: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void viewShowtime(ShowtimeDisplay display) {
        String info = String.format(
                "Phim: %s\n" +
                        "Rạp: %s\n" +
                        "Phòng: %s\n" +
                        "Ngày chiếu: %s\n" +
                        "Giờ chiếu: %s\n" +
                        "Thời lượng: %s\n" +
                        "Trạng thái: %s\n" +
                        "Ghế còn: %s",
                display.getMovieTitle(),
                display.getCinemaName(),
                display.getScreenName(),
                display.getDateStr(),
                display.getTimeStr(),
                display.getDurationStr(),
                display.getStatus(),
                display.getSeatsStr());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi Tiết Lịch Chiếu");
        alert.setHeaderText(display.getMovieTitle());
        alert.setContentText(info);
        alert.showAndWait();
    }

    private void editShowtime(ShowtimeDisplay display) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sửa Lịch Chiếu");
        alert.setHeaderText("Chức năng đang phát triển");
        alert.setContentText("Sửa lịch chiếu: " + display.getMovieTitle());
        alert.showAndWait();
    }

    private void deleteShowtime(ShowtimeDisplay display) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận Xóa");
        confirmAlert.setHeaderText("Bạn có chắc muốn xóa lịch chiếu này?");
        confirmAlert.setContentText(display.getMovieTitle() + " - " + display.getTimeStr());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showtimes.removeIf(s -> s.getId().equals(display.getShowtime().getId()));
                applyFilters();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Thành Công");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Đã xóa lịch chiếu thành công!");
                successAlert.showAndWait();
            }
        });
    }

    // Inner class for display
    public static class ShowtimeDisplay {
        private Showtime showtime;
        private Movie movie;
        private Cinema cinema;
        private Screen screen;
        private String dateStr;
        private String timeStr;
        private String movieTitle;
        private String cinemaName;
        private String screenName;
        private String durationStr;
        private String seatsStr;
        private String status;

        // Getters and setters
        public Showtime getShowtime() {
            return showtime;
        }

        public void setShowtime(Showtime showtime) {
            this.showtime = showtime;
        }

        public Movie getMovie() {
            return movie;
        }

        public void setMovie(Movie movie) {
            this.movie = movie;
        }

        public Cinema getCinema() {
            return cinema;
        }

        public void setCinema(Cinema cinema) {
            this.cinema = cinema;
        }

        public Screen getScreen() {
            return screen;
        }

        public void setScreen(Screen screen) {
            this.screen = screen;
        }

        public String getDateStr() {
            return dateStr;
        }

        public void setDateStr(String dateStr) {
            this.dateStr = dateStr;
        }

        public String getTimeStr() {
            return timeStr;
        }

        public void setTimeStr(String timeStr) {
            this.timeStr = timeStr;
        }

        public String getMovieTitle() {
            return movieTitle;
        }

        public void setMovieTitle(String movieTitle) {
            this.movieTitle = movieTitle;
        }

        public String getCinemaName() {
            return cinemaName;
        }

        public void setCinemaName(String cinemaName) {
            this.cinemaName = cinemaName;
        }

        public String getScreenName() {
            return screenName;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

        public String getDurationStr() {
            return durationStr;
        }

        public void setDurationStr(String durationStr) {
            this.durationStr = durationStr;
        }

        public String getSeatsStr() {
            return seatsStr;
        }

        public void setSeatsStr(String seatsStr) {
            this.seatsStr = seatsStr;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}