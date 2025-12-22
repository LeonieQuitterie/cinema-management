package com.cinema.controllers.admin.cinema;

import com.cinema.models.Cinema;
import com.cinema.models.Screen;
import com.cinema.models.Seat;
import com.cinema.models.SeatLayout;
import com.cinema.models.SeatType;
import com.cinema.utils.admin.CinemaApi;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CinemaListController implements Initializable {

    // Table fields
    @FXML private TableView<Cinema> cinemaTable;
    @FXML private TableColumn<Cinema, String> nameColumn;
    @FXML private TableColumn<Cinema, String> cityColumn;
    @FXML private TableColumn<Cinema, String> addressColumn;
    @FXML private TableColumn<Cinema, Integer> screenCountColumn;
    @FXML private TableColumn<Cinema, Void> actionColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> cityFilter;
    @FXML private Label totalCinemasLabel;
    @FXML private Label totalScreensLabel;

    // Seat Map fields
    @FXML private TextField rowsInput;
    @FXML private TextField colsInput;
    @FXML private GridPane seatGrid;
    @FXML private ScrollPane gridScrollPane;
    @FXML private Button normalSeatBtn;
    @FXML private Button vipSeatBtn;
    @FXML private Button coupleSeatBtn;
    @FXML private ComboBox<Cinema> cinemaSelector;
    @FXML private Label selectedCinemaLabel;
    @FXML private Label screenCountLabel;

    private ObservableList<Cinema> cinemaList = FXCollections.observableArrayList();
    private ObservableList<Cinema> allCinemas = FXCollections.observableArrayList();
    private SeatLayout currentSeatLayout;
    private SeatType selectedSeatType = SeatType.STANDARD;
    private boolean isDragging = false;

    // API Service
    private CinemaApi cinemaApi;
    private Cinema selectedCinemaForSeats;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cinemaApi = new CinemaApi();

        setupTableColumns();
        setupActionButtons();
        setupSeatMapButtons();
        setupFilters();
        setupCinemaSelector();
        loadCinemaData();
        // setupTableColumns();
        // loadCinemaData();
        // setupActionButtons();
        // setupSeatMapButtons();
    }

    // ====================== TABLE SETUP ======================
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        screenCountColumn.setCellValueFactory(new PropertyValueFactory<>("screenCount"));

        cinemaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionButtons() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Xem");
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final HBox pane = new HBox(8, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.getStyleClass().add("btn-view");
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

                viewBtn.setOnAction(event -> {
                    Cinema cinema = getTableView().getItems().get(getIndex());
                    if (cinema != null) viewCinemaDetail(cinema);
                });

                editBtn.setOnAction(event -> {
                    Cinema cinema = getTableView().getItems().get(getIndex());
                    if (cinema != null) openEditForm(cinema);
                });

                deleteBtn.setOnAction(event -> {
                    Cinema cinema = getTableView().getItems().get(getIndex());
                    if (cinema != null) deleteCinema(cinema);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        cityFilter.getItems().addAll(
            "Tất cả",
            "Hà Nội",
            "Hồ Chí Minh",
            "Đà Nẵng",
            "Cần Thơ",
            "Hải Phòng"
        );
        cityFilter.setValue("Tất cả");
        
        cityFilter.setOnAction(e -> applyFilters());
    }

    // ====================== LOAD DATA ======================
    private void loadCinemaData() {
        System.out.println("🎬 Loading cinemas from API...");
        
        cinemaApi.getAllCinemas()
            .thenAccept(cinemas -> {
                Platform.runLater(() -> {
                    System.out.println("✅ Loaded " + cinemas.size() + " cinemas");
                    allCinemas.clear();
                    allCinemas.addAll(cinemas);

                    // Update cinema selector
                    cinemaSelector.getItems().clear();
                    cinemaSelector.getItems().addAll(cinemas);
                    
                    applyFilters();
                    updateSummary();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println("❌ Error loading cinemas: " + ex.getMessage());
                    showError("Không thể tải danh sách rạp: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    private void updateSummary() {
        int totalCinemas = cinemaList.size();
        int totalScreens = cinemaList.stream().mapToInt(Cinema::getScreenCount).sum();

        totalCinemasLabel.setText("Tổng số rạp: " + totalCinemas);
        totalScreensLabel.setText("Tổng số phòng: " + totalScreens);
    }

    private void applyFilters() {
        cinemaList.clear();
        
        String selectedCity = cityFilter.getValue();
        String searchText = searchField.getText().toLowerCase().trim();
        
        for (Cinema cinema : allCinemas) {
            // City filter
            if (!selectedCity.equals("Tất cả") && !cinema.getCity().equals(selectedCity)) {
                continue;
            }
            
            // Search filter
            if (!searchText.isEmpty()) {
                boolean matches = cinema.getName().toLowerCase().contains(searchText) ||
                                cinema.getCity().toLowerCase().contains(searchText) ||
                                cinema.getAddress().toLowerCase().contains(searchText);
                if (!matches) {
                    continue;
                }
            }
            
            cinemaList.add(cinema);
        }
        
        cinemaTable.setItems(cinemaList);
        updateSummary();
    }

    // ====================== CINEMA ACTIONS ======================
    @FXML
    private void openCinemaForm() {
        openForm(null);
    }

    private void openEditForm(Cinema cinema) {
        openForm(cinema);
    }

    private void openForm(Cinema cinema) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/admin/partials/cinema/cinema-form-content.fxml"));
            Parent root = loader.load();

            CinemaFormController formController = loader.getController();
            formController.setData(cinema);
            formController.setOnSaveCallback(this::loadCinemaData);

            Stage stage = new Stage();
            stage.setTitle(cinema == null ? "Thêm Rạp Mới" : "Chỉnh Sửa Rạp");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(cinemaTable.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không mở được form: " + e.getMessage());
        }
    }

  private void viewCinemaDetail(Cinema cinema) {
    try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/admin/partials/cinema/cinema-detail-content.fxml"));
        Parent root = loader.load();

        CinemaDetailController detailController = loader.getController();
        detailController.setCinema(cinema);

        Stage stage = new Stage();
        stage.setTitle("Chi tiết rạp: " + cinema.getName());
        stage.setScene(new Scene(root, 1000, 700));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(cinemaTable.getScene().getWindow());
        stage.showAndWait();

    } catch (IOException e) {
        e.printStackTrace();
        showError("Không mở được chi tiết rạp: " + e.getMessage());
    }
}
    private void deleteCinema(Cinema cinema) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác Nhận Xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa rạp này?");
        alert.setContentText(
            cinema.getName() + "\n" +
            cinema.getAddress() + "\n\n" +
            "Cảnh báo: Hành động này không thể hoàn tác!"
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cinemaApi.deleteCinema(cinema.getId())
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            allCinemas.remove(cinema);
                            cinemaList.remove(cinema);
                            updateSummary();
                            showSuccess("Đã xóa rạp thành công!");
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            if (ex.getMessage().contains("HAS_SCREENS") || 
                                ex.getMessage().contains("có phòng chiếu")) {
                                showError("Không thể xóa rạp có phòng chiếu!\nVui lòng xóa tất cả phòng chiếu trước.");
                            } else if (ex.getMessage().contains("FOREIGN_KEY") || 
                                       ex.getMessage().contains("liên quan")) {
                                showError("Không thể xóa rạp có dữ liệu liên quan!");
                            } else {
                                showError("Không thể xóa rạp: " + ex.getMessage());
                            }
                        });
                        ex.printStackTrace();
                        return null;
                    });
            }
        });
    }

    @FXML
    private void searchCinema() {
        // String keyword = searchField.getText().toLowerCase().trim();

        // if (keyword.isEmpty()) {
        //     cinemaTable.setItems(cinemaList); // ✅ Set lại list gốc
        //     return;
        // }

        // // ✅ Tạo filtered list mới
        // ObservableList<Cinema> filtered = FXCollections.observableArrayList();

        // for (Cinema cinema : cinemaList) {
        //     if (cinema.getName().toLowerCase().contains(keyword) ||
        //             cinema.getCity().toLowerCase().contains(keyword) ||
        //             cinema.getAddress().toLowerCase().contains(keyword)) {
        //         filtered.add(cinema);
        //     }
        // }

        // cinemaTable.setItems(filtered);
        applyFilters();
    }
    // ====================== SEAT MAP METHODS ======================

    private void setupSeatMapButtons() {
        normalSeatBtn.getStyleClass().add("selected");
        selectedSeatType = SeatType.STANDARD;
    }

    @FXML
    private void selectNormalSeat() {
        selectedSeatType = SeatType.STANDARD;
        updateButtonSelection(normalSeatBtn);
    }

    @FXML
    private void selectVipSeat() {
        selectedSeatType = SeatType.VIP;
        updateButtonSelection(vipSeatBtn);
    }

    @FXML
    private void selectCoupleSeat() {
        selectedSeatType = SeatType.COUPLE;
        updateButtonSelection(coupleSeatBtn);
    }

    private void updateButtonSelection(Button selected) {
        normalSeatBtn.getStyleClass().remove("selected");
        vipSeatBtn.getStyleClass().remove("selected");
        coupleSeatBtn.getStyleClass().remove("selected");

        selected.getStyleClass().add("selected");
    }

    @FXML
    private void generateGrid() {
        try {
            int rows = Integer.parseInt(rowsInput.getText());
            int cols = Integer.parseInt(colsInput.getText());

            if (rows < 1 || rows > 20 || cols < 1 || cols > 30) {
                showError("Số hàng (1-20) và số cột (1-30) không hợp lệ!");
                return;
            }

            currentSeatLayout = new SeatLayout(rows, cols);
            renderSeatGrid();

        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số hợp lệ!");
        }
    }

    // ====================== CINEMA SELECTOR FOR SEAT MAP ======================
    private void setupCinemaSelector() {
        cinemaSelector.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Cinema item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        
        cinemaSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cinema item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "-- Chọn rạp --" : item.getName());
            }
        });
        
        cinemaSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            onCinemaSelected(newVal);
        });
    }

    private void onCinemaSelected(Cinema cinema) {
        selectedCinemaForSeats = cinema;
        
        if (cinema == null) {
            selectedCinemaLabel.setText("Chưa chọn rạp");
            screenCountLabel.setText("0 phòng");
            return;
        }
        
        System.out.println("🎬 Selected cinema for seat map: " + cinema.getName());
        
        // Load cinema details with screens
        cinemaApi.getCinemaById(cinema.getId())
            .thenAccept(loadedCinema -> {
                // System.out.println("loaded: "+loadedCinema.getScreens());
                Platform.runLater(() -> {
                    
                    selectedCinemaLabel.setText(loadedCinema.getName());
                    int screenCount = loadedCinema.getScreens() != null ? loadedCinema.getScreens().size() : 0;
                    screenCountLabel.setText(screenCount + " phòng");
                    
                    // Load first screen's seat layout if exists
                    if (screenCount > 0) {
                        Screen firstScreen = loadedCinema.getScreens().get(0);
                        loadSeatLayoutFromScreen(firstScreen);
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Không thể tải thông tin rạp: " + ex.getMessage());
                });
                ex.printStackTrace();
                return null;
            });
    }

    private void loadSeatLayoutFromScreen(Screen screen) {
        System.out.println("📐 Loading seat layout from screen: " + screen.getName());
        
        if (screen.getSeatLayout() == null) {
            showInfo("Thông báo", "Phòng chiếu chưa có sơ đồ ghế.\nBạn có thể tạo mới.");
            return;
        }
        
        SeatLayout layout = screen.getSeatLayout();
        
        // Set dimensions
        rowsInput.setText(String.valueOf(layout.getRows()));
        colsInput.setText(String.valueOf(layout.getColumns()));
        
        // Generate grid
        currentSeatLayout = new SeatLayout(layout.getRows(), layout.getColumns());
        
        // Load existing seats
        List<List<Seat>> seats = layout.getSeats();
        if (seats != null) {
            for (int row = 0; row < layout.getRows(); row++) {
                for (int col = 0; col < layout.getColumns(); col++) {
                    Seat apiSeat = seats.get(row).get(col);
                    if (apiSeat != null) {
                        SeatType seatType = apiSeat.getSeatType();
                        Seat seat = new Seat(
                            apiSeat.getSeatNumber(),
                            seatType,
                            apiSeat.getPrice(),
                            row,
                            col
                        );
                        currentSeatLayout.setSeat(row, col, seat);
                    }
                }
            }
        }
        
        renderSeatGrid();
        
        showInfo("Đã tải sơ đồ ghế", 
            String.format("Đã tải sơ đồ %d×%d từ %s", 
                layout.getRows(), 
                layout.getColumns(), 
                screen.getName()
            )
        );
    }

    private void renderSeatGrid() {
        seatGrid.getChildren().clear();

        for (int row = 0; row < currentSeatLayout.getRows(); row++) {
            for (int col = 0; col < currentSeatLayout.getColumns(); col++) {
                Region seatCell = createSeatCell(row, col);
                seatGrid.add(seatCell, col, row);
            }
        }
    }

    private Region createSeatCell(int row, int col) {
        Region cell = new Region();
        cell.getStyleClass().addAll("seat-cell", "seat-cell-empty");

        // Mouse pressed - start dragging
        cell.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isDragging = true;
                paintSeat(cell, row, col);
            }
        });

        // Mouse dragged - continue painting
        cell.setOnMouseDragEntered(event -> {
            if (isDragging) {
                paintSeat(cell, row, col);
            }
        });

        // Mouse released - stop dragging
        cell.setOnMouseReleased(event -> {
            isDragging = false;
        });

        // Enable drag detection
        cell.setOnDragDetected(event -> {
            cell.startFullDrag();
        });

        return cell;
    }

    private void paintSeat(Region cell, int row, int col) {
        // ✅ NGĂN TÔ GHẾ ĐÔI Ở CỘT CUỐI
        if (selectedSeatType == SeatType.COUPLE) {
            int nextCol = col + 1;
            if (nextCol >= currentSeatLayout.getColumns()) {
                // Đang ở cột cuối, không thể tạo ghế đôi
                showWarning("Không thể tạo ghế đôi ở cột cuối cùng!\nGhế đôi cần 2 ô liên tiếp.");
                return; // ✅ DỪNG LẠI, KHÔNG TÔ
            }
        }

        // XÓA TẤT CẢ STYLE CŨ
        cell.getStyleClass().removeAll("seat-cell-normal", "seat-cell-vip",
                "seat-cell-couple", "seat-cell-empty");

        // TẠO GHẾ VỚI LOẠI ĐÃ CHỌN
        String seatNumber = generateSeatNumber(row, col);
        double price = getPriceForSeatType(selectedSeatType);

        Seat seat = new Seat(seatNumber, selectedSeatType, price, row, col);
        currentSeatLayout.setSeat(row, col, seat);

        // THÊM STYLE MỚI THEO LOẠI GHẾ
        switch (selectedSeatType) {
            case STANDARD:
                cell.getStyleClass().add("seat-cell-normal");
                break;

            case VIP:
                cell.getStyleClass().add("seat-cell-vip");
                break;

            case COUPLE:
                cell.getStyleClass().add("seat-cell-couple");

                // ✅ TÔ LUÔN Ô BÊN PHẢI (đã check ở trên rồi, chắc chắn có ô bên phải)
                int nextCol = col + 1;

                // Tạo ghế cho ô bên phải
                String nextSeatNumber = generateSeatNumber(row, nextCol);
                Seat nextSeat = new Seat(nextSeatNumber, selectedSeatType, price, row, nextCol);
                currentSeatLayout.setSeat(row, nextCol, nextSeat);

                // Tìm và tô màu cho ô bên phải trong GridPane
                Region nextCell = findCellInGrid(row, nextCol);
                if (nextCell != null) {
                    nextCell.getStyleClass().removeAll("seat-cell-normal", "seat-cell-vip",
                            "seat-cell-couple", "seat-cell-empty");
                    nextCell.getStyleClass().add("seat-cell-couple");
                }
                break;
        }
    }

    // ✅ THÊM METHOD MỚI: Tìm cell trong GridPane theo vị trí
    private Region findCellInGrid(int row, int col) {
        for (javafx.scene.Node node : seatGrid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);

            int actualRow = (nodeRow == null) ? 0 : nodeRow;
            int actualCol = (nodeCol == null) ? 0 : nodeCol;

            if (actualRow == row && actualCol == col && node instanceof Region) {
                return (Region) node;
            }
        }
        return null;
    }

    private String generateSeatNumber(int row, int col) {
        char rowLetter = (char) ('A' + row);
        int colNumber = col + 1;
        return rowLetter + String.valueOf(colNumber);
    }

    private double getPriceForSeatType(SeatType seatType) {
        switch (seatType) {
            case STANDARD:
                return 75000;
            case VIP:
                return 120000;
            case COUPLE:
                return 200000;
            default:
                return 0;
        }
    }

    @FXML
    private void resetMap() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc muốn đặt lại sơ đồ ghế?");
        alert.setContentText("Tất cả thay đổi sẽ bị xóa.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && currentSeatLayout != null) {
                generateGrid();
                showSuccess("Đã đặt lại sơ đồ ghế!");
            }
        });
    }

    @FXML
    private void saveSeatMap() {
        if (currentSeatLayout == null) {
            showError("Vui lòng tạo sơ đồ ghế trước!");
            return;
        }
        
        if (selectedCinemaForSeats == null) {
            showError("Vui lòng chọn rạp trước!");
            return;
        }
        
        if (selectedCinemaForSeats.getScreens() == null || selectedCinemaForSeats.getScreens().isEmpty()) {
            showError("Rạp chưa có phòng chiếu!");
            return;
        }
        
        // Count seats
        SeatCount count = new SeatCount();
        
        List<CinemaApi.SeatData> seatDataList = new ArrayList<>();
        
        for (int row = 0; row < currentSeatLayout.getRows(); row++) {
            for (int col = 0; col < currentSeatLayout.getColumns(); col++) {
                Seat seat = currentSeatLayout.getSeat(row, col);
                
                if (seat == null) {
                    count.aisle++;
                    seatDataList.add(null);
                } else {
                    switch (seat.getSeatType()) {
                        case STANDARD -> count.standard++;
                        case VIP -> count.vip++;
                        case COUPLE -> count.couple++;
                    }
                    
                    seatDataList.add(new CinemaApi.SeatData(
                        seat.getSeatNumber(),
                        seat.getSeatType().name(),
                        seat.getPrice()
                    ));
                }
            }
        }
        
        // Confirm before saving
        int screenCount = selectedCinemaForSeats.getScreens().size();
        String confirmMsg = String.format(
            "Bạn có chắc muốn áp dụng sơ đồ ghế này cho TẤT CẢ %d phòng chiếu?\n\n" +
            "📐 Kích thước: %d hàng × %d cột\n" +
            "🔵 Ghế thường: %d\n" +
            "🟡 Ghế VIP: %d\n" +
            "🔴 Ghế đôi: %d\n" +
            "⬜ Lối đi: %d\n\n" +
            "Cảnh báo: Sơ đồ ghế cũ sẽ bị thay thế!",
            screenCount,
            currentSeatLayout.getRows(),
            currentSeatLayout.getColumns(),
            count.standard, count.vip, count.couple, count.aisle
        );
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận Lưu");
        confirmAlert.setHeaderText("Áp dụng cho tất cả phòng chiếu");
        confirmAlert.setContentText(confirmMsg);
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Save via API
                CinemaApi.SeatLayoutData layoutData = new CinemaApi.SeatLayoutData(
                    currentSeatLayout.getRows(),
                    currentSeatLayout.getColumns(),
                    seatDataList
                );
                
                cinemaApi.updateBulkSeatLayout(selectedCinemaForSeats.getId(), layoutData)
                    .thenAccept(screensUpdated -> {
                        Platform.runLater(() -> {
                            String summary = String.format(
                                "✅ Đã lưu sơ đồ ghế thành công!\n\n" +
                                "📍 Rạp: %s\n" +
                                "🎭 Số phòng đã cập nhật: %d\n" +
                                "📐 Kích thước: %d hàng × %d cột\n\n" +
                                "🔵 Ghế thường: %d\n" +
                                "🟡 Ghế VIP: %d\n" +
                                "🔴 Ghế đôi: %d\n" +
                                "⬜ Lối đi: %d",
                                selectedCinemaForSeats.getName(),
                                screensUpdated,
                                currentSeatLayout.getRows(),
                                currentSeatLayout.getColumns(),
                                count.standard, count.vip, count.couple, count.aisle
                            );
                            
                            showSuccess(summary);
                            
                            // Reload cinema data
                            loadCinemaData();
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            showError("Không thể lưu sơ đồ ghế: " + ex.getMessage());
                        });
                        ex.printStackTrace();
                        return null;
                    });
            }
        });
    }

    // ====================== HELPERS ======================
    // ✅ THÊM METHOD HIỂN THỊ CẢNH BÁO (không chặn UI)
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Dùng show() thay vì showAndWait()
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    static class SeatCount {
        int standard;
        int vip;
        int couple;
        int aisle;
    }
}