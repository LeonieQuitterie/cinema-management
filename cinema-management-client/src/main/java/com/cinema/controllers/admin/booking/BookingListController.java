package com.cinema.controllers.admin.booking;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.NumberFormat;

public class BookingListController implements Initializable {
    
    // Header
    @FXML private Label bookingCountLabel;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    
    // Stats
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label paidBookingsLabel;
    @FXML private Label pendingBookingsLabel;
    @FXML private Label cancelledBookingsLabel;
    
    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> cinemaFilterCombo;
    @FXML private ComboBox<String> movieFilterCombo;
    
    // Table
    @FXML private TableView<BookingRow> bookingTable;
    @FXML private TableColumn<BookingRow, String> bookingIdColumn;
    @FXML private TableColumn<BookingRow, String> bookingTimeColumn;
    @FXML private TableColumn<BookingRow, String> movieColumn;
    @FXML private TableColumn<BookingRow, String> cinemaColumn;
    @FXML private TableColumn<BookingRow, String> screenColumn;
    @FXML private TableColumn<BookingRow, String> showtimeColumn;
    @FXML private TableColumn<BookingRow, String> customerColumn;
    @FXML private TableColumn<BookingRow, String> phoneColumn;
    @FXML private TableColumn<BookingRow, String> seatsColumn;
    @FXML private TableColumn<BookingRow, String> totalPriceColumn;
    @FXML private TableColumn<BookingRow, String> statusColumn;
    @FXML private TableColumn<BookingRow, Void> actionsColumn;
    
    // Pagination
    @FXML private Button firstPageBtn;
    @FXML private Button prevPageBtn;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageBtn;
    @FXML private Button lastPageBtn;
    @FXML private ComboBox<Integer> rowsPerPageCombo;
    
    private NumberFormat currencyFormat;
    private ObservableList<BookingRow> allBookings;
    private ObservableList<BookingRow> filteredBookings;
    
    private int currentPage = 1;
    private int rowsPerPage = 20;
    private int totalPages = 1;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        setupFilters();
        setupTable();
        setupPagination();
        
        loadBookings();
        updateStats();
    }
    
    private void setupFilters() {
        // Status filter
        ObservableList<String> statuses = FXCollections.observableArrayList(
            "Tất cả", "Đã thanh toán", "Chờ thanh toán", "Hết hạn", "Đã hủy"
        );
        statusFilterCombo.setItems(statuses);
        statusFilterCombo.setValue("Tất cả");
        statusFilterCombo.setOnAction(e -> applyFilters());
        
        // Cinema filter
        ObservableList<String> cinemas = FXCollections.observableArrayList(
            "Tất cả", "CGV Vincom", "Lotte Cinema", "Galaxy Cinema", "BHD Star", "Platinum"
        );
        cinemaFilterCombo.setItems(cinemas);
        cinemaFilterCombo.setValue("Tất cả");
        cinemaFilterCombo.setOnAction(e -> applyFilters());
        
        // Movie filter
        ObservableList<String> movies = FXCollections.observableArrayList(
            "Tất cả", "Avatar 3", "Inception 2", "The Matrix 5", "Dune Part 3", "Spider-Man 4"
        );
        movieFilterCombo.setItems(movies);
        movieFilterCombo.setValue("Tất cả");
        movieFilterCombo.setOnAction(e -> applyFilters());
        
        // Date pickers
        fromDatePicker.setOnAction(e -> applyFilters());
        toDatePicker.setOnAction(e -> applyFilters());
    }
    
    private void setupTable() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookingTimeColumn.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        movieColumn.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        cinemaColumn.setCellValueFactory(new PropertyValueFactory<>("cinemaName"));
        screenColumn.setCellValueFactory(new PropertyValueFactory<>("screenName"));
        showtimeColumn.setCellValueFactory(new PropertyValueFactory<>("showtime"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Status column with styled badges
        statusColumn.setCellFactory(column -> new TableCell<BookingRow, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("status-badge");
                    
                    if (status.contains("Đã thanh toán")) {
                        badge.getStyleClass().add("status-paid");
                    } else if (status.contains("Chờ")) {
                        badge.getStyleClass().add("status-pending");
                    } else if (status.contains("Hết hạn")) {
                        badge.getStyleClass().add("status-expired");
                    } else if (status.contains("Đã hủy")) {
                        badge.getStyleClass().add("status-cancelled");
                    }
                    
                    setGraphic(badge);
                }
            }
        });
        
        // Actions column with buttons
        actionsColumn.setCellFactory(column -> new TableCell<BookingRow, Void>() {
            private final Button detailBtn = new Button("Chi tiết");
            private final Button printBtn = new Button("In vé");
            private final HBox buttons = new HBox(5, detailBtn, printBtn);
            
            {
                detailBtn.getStyleClass().addAll("action-button", "print-btn");
                printBtn.getStyleClass().addAll("action-button", "email-btn");
                
                detailBtn.setOnAction(event -> {
                    BookingRow booking = getTableRow().getItem();
                    if (booking != null) {
                        openBookingDetail(booking);
                    }
                });
                
                printBtn.setOnAction(event -> {
                    BookingRow booking = getTableRow().getItem();
                    if (booking != null) {
                        handlePrintTicket(booking);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        // Row click to open detail
        bookingTable.setRowFactory(tv -> {
            TableRow<BookingRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openBookingDetail(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void setupPagination() {
        ObservableList<Integer> rowOptions = FXCollections.observableArrayList(10, 20, 50, 100);
        rowsPerPageCombo.setItems(rowOptions);
        rowsPerPageCombo.setValue(20);
        rowsPerPageCombo.setOnAction(e -> {
            rowsPerPage = rowsPerPageCombo.getValue();
            currentPage = 1;
            updateTableView();
        });
    }
    
    private void loadBookings() {
        // TODO: Replace with actual service call
        allBookings = FXCollections.observableArrayList();
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yy");
        
        // Mock data
        allBookings.add(new BookingRow("BK001", 
            LocalDateTime.now().minusHours(1).format(dtf),
            "Avatar 3", "CGV Vincom", "P1", 
            LocalDateTime.now().plusHours(2).format(dtf),
            "Nguyễn Văn A", "0901234567", "A1-A4",
            formatCurrency(560000), "Đã thanh toán"));
            
        allBookings.add(new BookingRow("BK002", 
            LocalDateTime.now().minusHours(2).format(dtf),
            "Inception 2", "Lotte Cinema", "P3", 
            LocalDateTime.now().plusHours(3).format(dtf),
            "Trần Thị B", "0907654321", "C5-C7",
            formatCurrency(525000), "Chờ thanh toán"));
            
        allBookings.add(new BookingRow("BK003", 
            LocalDateTime.now().minusHours(3).format(dtf),
            "The Matrix 5", "Galaxy Cinema", "P2", 
            LocalDateTime.now().plusHours(1).format(dtf),
            "Lê Văn C", "0912345678", "E10",
            formatCurrency(175000), "Đã thanh toán"));
            
        allBookings.add(new BookingRow("BK004", 
            LocalDateTime.now().minusHours(5).format(dtf),
            "Dune Part 3", "BHD Star", "P4", 
            LocalDateTime.now().minusHours(1).format(dtf),
            "Phạm Thị D", "0923456789", "B3-B4",
            formatCurrency(400000), "Hết hạn"));
            
        allBookings.add(new BookingRow("BK005", 
            LocalDateTime.now().minusDays(1).format(dtf),
            "Spider-Man 4", "Platinum", "P1", 
            LocalDateTime.now().minusHours(12).format(dtf),
            "Hoàng Văn E", "0934567890", "D8-D9",
            formatCurrency(380000), "Đã hủy"));
        
        // Add more mock data
        for (int i = 6; i <= 50; i++) {
            allBookings.add(new BookingRow("BK" + String.format("%03d", i), 
                LocalDateTime.now().minusHours(i).format(dtf),
                "Avatar 3", "CGV Vincom", "P" + (i % 5 + 1), 
                LocalDateTime.now().plusHours(i % 24).format(dtf),
                "Khách hàng " + i, "090123456" + (i % 10), "A" + (i % 10),
                formatCurrency(150000 + i * 10000), "Đã thanh toán"));
        }
        
        filteredBookings = FXCollections.observableArrayList(allBookings);
        updateTableView();
    }
    
    @FXML
    private void handleSearch() {
        applyFilters();
    }
    
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterCombo.setValue("Tất cả");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        cinemaFilterCombo.setValue("Tất cả");
        movieFilterCombo.setValue("Tất cả");
        applyFilters();
    }
    
    private void applyFilters() {
        filteredBookings.clear();
        
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = statusFilterCombo.getValue();
        String cinemaFilter = cinemaFilterCombo.getValue();
        String movieFilter = movieFilterCombo.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        for (BookingRow booking : allBookings) {
            boolean matches = true;
            
            // Search filter
            if (!searchText.isEmpty()) {
                matches = booking.getBookingId().toLowerCase().contains(searchText) ||
                         booking.getCustomerName().toLowerCase().contains(searchText) ||
                         booking.getPhone().contains(searchText);
            }
            
            // Status filter
            if (matches && !statusFilter.equals("Tất cả")) {
                matches = booking.getStatus().equals(statusFilter);
            }
            
            // Cinema filter
            if (matches && !cinemaFilter.equals("Tất cả")) {
                matches = booking.getCinemaName().equals(cinemaFilter);
            }
            
            // Movie filter
            if (matches && !movieFilter.equals("Tất cả")) {
                matches = booking.getMovieTitle().equals(movieFilter);
            }
            
            // Date filters would need actual LocalDateTime comparison
            // TODO: Implement date filtering
            
            if (matches) {
                filteredBookings.add(booking);
            }
        }
        
        currentPage = 1;
        updateTableView();
        updateStats();
    }
    
    private void updateTableView() {
        totalPages = (int) Math.ceil((double) filteredBookings.size() / rowsPerPage);
        if (totalPages == 0) totalPages = 1;
        
        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, filteredBookings.size());
        
        ObservableList<BookingRow> pageData = FXCollections.observableArrayList(
            filteredBookings.subList(fromIndex, toIndex)
        );
        
        bookingTable.setItems(pageData);
        pageInfoLabel.setText(String.format("Trang %d / %d", currentPage, totalPages));
        bookingCountLabel.setText(filteredBookings.size() + " đơn hàng");
        
        // Update button states
        firstPageBtn.setDisable(currentPage == 1);
        prevPageBtn.setDisable(currentPage == 1);
        nextPageBtn.setDisable(currentPage == totalPages);
        lastPageBtn.setDisable(currentPage == totalPages);
    }
    
    private void updateStats() {
        int total = filteredBookings.size();
        int paid = 0;
        int pending = 0;
        int cancelled = 0;
        double revenue = 0;
        
        for (BookingRow booking : filteredBookings) {
            String status = booking.getStatus();
            if (status.contains("Đã thanh toán")) {
                paid++;
                revenue += parsePrice(booking.getTotalPrice());
            } else if (status.contains("Chờ")) {
                pending++;
            } else if (status.contains("Đã hủy")) {
                cancelled++;
            }
        }
        
        totalBookingsLabel.setText(String.valueOf(total));
        totalRevenueLabel.setText(formatCurrency(revenue));
        paidBookingsLabel.setText(String.valueOf(paid));
        pendingBookingsLabel.setText(String.valueOf(pending));
        cancelledBookingsLabel.setText(String.valueOf(cancelled));
    }
    
    @FXML
    private void handleRefresh() {
        loadBookings();
        updateStats();
    }
    
    @FXML
    private void handleExport() {
        // TODO: Implement Excel export
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Xuất Excel");
        alert.setHeaderText(null);
        alert.setContentText("Chức năng xuất Excel đang được phát triển");
        alert.showAndWait();
    }
    
    @FXML
    private void handleFirstPage() {
        currentPage = 1;
        updateTableView();
    }
    
    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTableView();
        }
    }
    
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updateTableView();
        }
    }
    
    @FXML
    private void handleLastPage() {
        currentPage = totalPages;
        updateTableView();
    }
    
    private void openBookingDetail(BookingRow booking) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/views/admin/partials/booking/booking-detail-content.fxml"));
            Parent root = loader.load();
            
            BookingDetailController controller = loader.getController();
            controller.setBooking(booking);
            
            Stage stage = new Stage();
            stage.setTitle("Chi tiết đơn hàng");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh after closing detail
            handleRefresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handlePrintTicket(BookingRow booking) {
        // TODO: Implement print ticket
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("In vé");
        alert.setHeaderText("In vé đơn hàng " + booking.getBookingId());
        alert.setContentText("Chức năng in vé đang được phát triển");
        alert.showAndWait();
    }
    
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount) + "đ";
    }
    
    private double parsePrice(String priceString) {
        String numberOnly = priceString.replace("đ", "").replace(".", "").replace(",", "").trim();
        try {
            return Double.parseDouble(numberOnly);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // Inner class for table row
    public static class BookingRow {
        private String bookingId;
        private String bookingTime;
        private String movieTitle;
        private String cinemaName;
        private String screenName;
        private String showtime;
        private String customerName;
        private String phone;
        private String seats;
        private String totalPrice;
        private String status;
        
        public BookingRow(String bookingId, String bookingTime, String movieTitle,
                         String cinemaName, String screenName, String showtime,
                         String customerName, String phone, String seats,
                         String totalPrice, String status) {
            this.bookingId = bookingId;
            this.bookingTime = bookingTime;
            this.movieTitle = movieTitle;
            this.cinemaName = cinemaName;
            this.screenName = screenName;
            this.showtime = showtime;
            this.customerName = customerName;
            this.phone = phone;
            this.seats = seats;
            this.totalPrice = totalPrice;
            this.status = status;
        }
        
        // Getters
        public String getBookingId() { return bookingId; }
        public String getBookingTime() { return bookingTime; }
        public String getMovieTitle() { return movieTitle; }
        public String getCinemaName() { return cinemaName; }
        public String getScreenName() { return screenName; }
        public String getShowtime() { return showtime; }
        public String getCustomerName() { return customerName; }
        public String getPhone() { return phone; }
        public String getSeats() { return seats; }
        public String getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
    }
}