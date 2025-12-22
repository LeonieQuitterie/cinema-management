package com.cinema.controllers.admin.cinema;

import com.cinema.models.Cinema;
import com.cinema.models.Screen;
import com.cinema.utils.admin.CinemaApi;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CinemaDetailController implements Initializable {

    @FXML private Label cinemaNameLabel;
    @FXML private Label cinemaAddressLabel;
    @FXML private Label cinemaCityLabel;
    @FXML private Label totalScreensLabel;
    @FXML private Label totalSeatsLabel;

    @FXML private TableView<Screen> screenTable;
    @FXML private TableColumn<Screen, String> screenNameColumn;
    @FXML private TableColumn<Screen, String> screenIdColumn;
    @FXML private TableColumn<Screen, Integer> totalSeatsColumn;
    @FXML private TableColumn<Screen, Void> actionColumn;

    @FXML private TextField screenNameInput;

    private Cinema cinema;
    private ObservableList<Screen> screenList = FXCollections.observableArrayList();
    private CinemaApi cinemaApi;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cinemaApi = new CinemaApi();

        setupTableColumns();
        setupActionButtons();
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;

        // If cinema has ID, load fresh data from API
        if (cinema.getId() != null) {
            loadCinemaData(cinema.getId());
        } else {
            // Otherwise use provided data
            displayCinemaData();
        }
        // loadCinemaData();
        // loadScreenData();
    }

    private void loadCinemaData(String cinemaId) {
        System.out.println("🎬 Loading cinema details: " + cinemaId);

        // cinemaNameLabel.setText(cinema.getName());
        // cinemaAddressLabel.setText(cinema.getAddress());
        // cinemaCityLabel.setText(cinema.getCity());
        // updateSummary();
        cinemaApi.getCinemaById(cinemaId)
            .thenAccept(loadedCinema -> {
                Platform.runLater(() -> {
                    System.out.println("✅ Cinema loaded: " + loadedCinema.getName());
                    this.cinema = loadedCinema;
                    displayCinemaData();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println("❌ Failed to load cinema: " + ex.getMessage());
                    showError("Không thể tải thông tin rạp: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    private void displayCinemaData() {
        cinemaNameLabel.setText(cinema.getName());
        cinemaAddressLabel.setText(cinema.getAddress());
        cinemaCityLabel.setText(cinema.getCity());
        
        screenList.clear();
        if (cinema.getScreens() != null) {
            screenList.addAll(cinema.getScreens());
        }
        screenTable.setItems(screenList);
        
        updateSummary();
    }

    // private void loadScreenData() {
    //     screenList.clear();
    //     if (cinema.getScreens() != null) {
    //         screenList.addAll(cinema.getScreens());
    //     }
    //     screenTable.setItems(screenList);
    //     updateSummary();
    // }

    private void setupTableColumns() {
        screenNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        screenIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        totalSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));

        screenTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionButtons() {

        // Action Column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(event -> {
                    Screen screen = getTableView().getItems().get(getIndex());
                    editScreen(screen);
                });

                deleteBtn.setOnAction(event -> {
                    Screen screen = getTableView().getItems().get(getIndex());
                    deleteScreen(screen);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void updateSummary() {
        int totalScreens = screenList.size();
        int totalSeats = screenList.stream()
                .mapToInt(Screen::getTotalSeats)
                .sum();

        totalScreensLabel.setText(String.valueOf(totalScreens));
        totalSeatsLabel.setText(String.valueOf(totalSeats));
    }

    @FXML
    private void addNewScreen() {
        // TODO: Mở form thêm phòng mới
        showInfo("Chức năng đang phát triển", "Form thêm phòng mới sẽ được triển khai");
    }

    private void editScreen(Screen screen) {
        // TODO: Mở form sửa phòng
        showInfo("Chức năng đang phát triển", "Form sửa phòng: " + screen.getName());
    }

    @FXML
    private void addScreenQuick() {
        String screenName = screenNameInput.getText().trim();

        if (screenName.isEmpty()) {
            showInfo("Thiếu thông tin", "Vui lòng nhập tên phòng chiếu");
            return;
        }

        // Tạo Screen đơn giản (Q có thể đổi constructor theo model thật)
        Screen newScreen = new Screen();
        newScreen.setName(screenName);
        newScreen.setId("SCR-" + (screenList.size() + 1));
        newScreen.setTotalSeats(0);

        // Add vào cinema + table
        screenList.add(newScreen);
        cinema.getScreens().add(newScreen);

        // Reset input
        screenNameInput.clear();
        updateSummary();

        showSuccess("Đã thêm phòng: " + screenName);
    }

    private void deleteScreen(Screen screen) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác Nhận Xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa phòng chiếu này?");
        alert.setContentText(screen.getName() + " (" + screen.getTotalSeats() + " ghế)");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                screenList.remove(screen);
                cinema.getScreens().remove(screen);
                updateSummary();
                showSuccess("Đã xóa phòng chiếu thành công!");
            }
        });
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) cinemaNameLabel.getScene().getWindow();
        stage.close();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}