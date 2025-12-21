package com.cinema.controllers.admin.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.NumberFormat;

public class DashboardController implements Initializable {
    
    // Header
    @FXML private Label currentDateLabel;
    @FXML private Button refreshButton;
    
    // Stats Cards
    @FXML private Label todayRevenueLabel;
    @FXML private Label revenueChangeLabel;
    @FXML private Label todayTicketsLabel;
    @FXML private Label ticketsChangeLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label newCustomersLabel;
    @FXML private Label activeMoviesLabel;
    @FXML private Label upcomingMoviesLabel;
    
    // Charts
    @FXML private StackPane revenueChartPane;
    @FXML private StackPane paymentStatusChartPane;
    @FXML private ComboBox<String> topMoviesPeriodCombo;
    @FXML private StackPane topMoviesChartPane;
    @FXML private StackPane seatTypeChartPane;
    @FXML private StackPane bookingByHourChartPane;
    @FXML private StackPane movieStatusChartPane;
    @FXML private StackPane genreChartPane;
    @FXML private Label comboRevenueLabel;
    @FXML private StackPane topCombosChartPane;
    @FXML private StackPane revenueByCinemaChartPane;
    @FXML private StackPane ratingBreakdownChartPane;
    
    // Recent Bookings Table
    @FXML private Label recentBookingsCountLabel;
    @FXML private TableView<BookingRow> recentBookingsTable;
    @FXML private TableColumn<BookingRow, String> bookingIdColumn;
    @FXML private TableColumn<BookingRow, String> bookingMovieColumn;
    @FXML private TableColumn<BookingRow, String> bookingCinemaColumn;
    @FXML private TableColumn<BookingRow, String> bookingScreenColumn;
    @FXML private TableColumn<BookingRow, String> bookingCustomerColumn;
    @FXML private TableColumn<BookingRow, String> bookingSeatsColumn;
    @FXML private TableColumn<BookingRow, String> bookingShowtimeColumn;
    @FXML private TableColumn<BookingRow, String> bookingPriceColumn;
    @FXML private TableColumn<BookingRow, String> bookingStatusColumn;
    
    private NumberFormat currencyFormat;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        updateCurrentDate();
        setupPeriodCombo();
        setupRecentBookingsTable();
        
        loadDashboardData();
    }
    
    private void updateCurrentDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("vi", "VN"));
        currentDateLabel.setText(today.format(formatter));
    }
    
    private void setupPeriodCombo() {
        ObservableList<String> periods = FXCollections.observableArrayList(
            "Hôm nay", "7 ngày qua", "30 ngày qua", "Tháng này"
        );
        topMoviesPeriodCombo.setItems(periods);
        topMoviesPeriodCombo.setValue("7 ngày qua");
        topMoviesPeriodCombo.setOnAction(e -> loadTopMoviesChart());
    }
    
    private void setupRecentBookingsTable() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookingMovieColumn.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        bookingCinemaColumn.setCellValueFactory(new PropertyValueFactory<>("cinemaName"));
        bookingScreenColumn.setCellValueFactory(new PropertyValueFactory<>("screenName"));
        bookingCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        bookingSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        bookingShowtimeColumn.setCellValueFactory(new PropertyValueFactory<>("showtime"));
        bookingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Style columns
        bookingStatusColumn.setCellFactory(column -> new TableCell<BookingRow, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.contains("Đã thanh toán")) {
                        setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                    } else if (status.contains("Chờ")) {
                        setStyle("-fx-text-fill: #ffa500; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        bookingPriceColumn.setCellFactory(column -> new TableCell<BookingRow, String>() {
            @Override
            protected void updateItem(String price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(price);
                    setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        loadStatsCards();
        loadRevenueChart();
        loadPaymentStatusChart();
        loadTopMoviesChart();
        loadSeatTypeChart();
        loadBookingByHourChart();
        loadMovieStatusChart();
        loadGenreChart();
        loadTopCombosChart();
        loadRevenueByCinemaChart();
        loadRatingBreakdownChart();
        loadRecentBookings();
    }
    
    private void loadStatsCards() {
        // TODO: Replace with actual service calls
        double todayRevenue = 15750000;
        int todayTickets = 42;
        int totalCustomers = 1250;
        int newCustomers = 8;
        int activeMovies = 12;
        int upcomingMovies = 5;
        
        todayRevenueLabel.setText(formatCurrency(todayRevenue));
        revenueChangeLabel.setText("+12.5% so với hôm qua");
        todayTicketsLabel.setText(String.valueOf(todayTickets));
        ticketsChangeLabel.setText("+8.3% so với hôm qua");
        totalCustomersLabel.setText(String.valueOf(totalCustomers));
        newCustomersLabel.setText(newCustomers + " khách mới hôm nay");
        activeMoviesLabel.setText(String.valueOf(activeMovies));
        upcomingMoviesLabel.setText(upcomingMovies + " phim sắp chiếu");
    }
    
    private void loadRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Triệu đồng");
        
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle("");
        areaChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("T2", 12.5));
        series.getData().add(new XYChart.Data<>("T3", 14.2));
        series.getData().add(new XYChart.Data<>("T4", 11.8));
        series.getData().add(new XYChart.Data<>("T5", 16.5));
        series.getData().add(new XYChart.Data<>("T6", 18.3));
        series.getData().add(new XYChart.Data<>("T7", 22.1));
        series.getData().add(new XYChart.Data<>("CN", 19.7));
        
        areaChart.getData().add(series);
        areaChart.setStyle("-fx-background-color: transparent;");
        
        revenueChartPane.getChildren().clear();
        revenueChartPane.getChildren().add(areaChart);
    }
    
    private void loadPaymentStatusChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Đã thanh toán (156)", 156),
            new PieChart.Data("Chờ thanh toán (8)", 8),
            new PieChart.Data("Hết hạn (3)", 3),
            new PieChart.Data("Đã hủy (2)", 2)
        );
        
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("");
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setStyle("-fx-background-color: transparent;");
        
        paymentStatusChartPane.getChildren().clear();
        paymentStatusChartPane.getChildren().add(pieChart);
    }
    
    private void loadTopMoviesChart() {
        CategoryAxis yAxis = new CategoryAxis();
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Số vé bán");
        
        BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(256, "Avatar 3"));
        series.getData().add(new XYChart.Data<>(198, "Inception 2"));
        series.getData().add(new XYChart.Data<>(187, "The Matrix 5"));
        series.getData().add(new XYChart.Data<>(165, "Dune Part 3"));
        series.getData().add(new XYChart.Data<>(142, "Spider-Man 4"));
        series.getData().add(new XYChart.Data<>(128, "Interstellar 2"));
        series.getData().add(new XYChart.Data<>(115, "Oppenheimer 2"));
        series.getData().add(new XYChart.Data<>(98, "The Batman 2"));
        series.getData().add(new XYChart.Data<>(87, "Joker 3"));
        series.getData().add(new XYChart.Data<>(76, "Barbie 2"));
        
        barChart.getData().add(series);
        barChart.setStyle("-fx-background-color: transparent;");
        
        topMoviesChartPane.getChildren().clear();
        topMoviesChartPane.getChildren().add(barChart);
    }
    
    private void loadSeatTypeChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Ghế thường (520)", 520),
            new PieChart.Data("Ghế VIP (180)", 180),
            new PieChart.Data("Ghế đôi (45)", 45)
        );
        
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("");
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.setStyle("-fx-background-color: transparent;");
        
        seatTypeChartPane.getChildren().clear();
        seatTypeChartPane.getChildren().add(pieChart);
    }
    
    private void loadBookingByHourChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Giờ");
        yAxis.setLabel("Số vé");
        
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle("");
        areaChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("8h", 5));
        series.getData().add(new XYChart.Data<>("10h", 12));
        series.getData().add(new XYChart.Data<>("12h", 28));
        series.getData().add(new XYChart.Data<>("14h", 35));
        series.getData().add(new XYChart.Data<>("16h", 42));
        series.getData().add(new XYChart.Data<>("18h", 65));
        series.getData().add(new XYChart.Data<>("20h", 78));
        series.getData().add(new XYChart.Data<>("22h", 52));
        
        areaChart.getData().add(series);
        areaChart.setStyle("-fx-background-color: transparent;");
        
        bookingByHourChartPane.getChildren().clear();
        bookingByHourChartPane.getChildren().add(areaChart);
    }
    
    private void loadMovieStatusChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Đang chiếu (12)", 12),
            new PieChart.Data("Sắp chiếu (5)", 5),
            new PieChart.Data("Ngừng chiếu (3)", 3)
        );
        
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("");
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.setStyle("-fx-background-color: transparent;");
        
        movieStatusChartPane.getChildren().clear();
        movieStatusChartPane.getChildren().add(pieChart);
    }
    
    private void loadGenreChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Hành động (8)", 8),
            new PieChart.Data("Khoa học viễn tưởng (5)", 5),
            new PieChart.Data("Kinh dị (3)", 3),
            new PieChart.Data("Hài (2)", 2),
            new PieChart.Data("Tâm lý (2)", 2)
        );
        
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("");
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setStyle("-fx-background-color: transparent;");
        
        genreChartPane.getChildren().clear();
        genreChartPane.getChildren().add(pieChart);
    }
    
    private void loadTopCombosChart() {
        CategoryAxis yAxis = new CategoryAxis();
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Số lượng bán");
        
        BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(203, "Coca Cola L"));
        series.getData().add(new XYChart.Data<>(156, "Bắp rang bơ lớn"));
        series.getData().add(new XYChart.Data<>(89, "Combo Couple Sweet"));
        series.getData().add(new XYChart.Data<>(78, "Combo Solo"));
        series.getData().add(new XYChart.Data<>(67, "Combo Family"));
        series.getData().add(new XYChart.Data<>(54, "Pepsi L"));
        series.getData().add(new XYChart.Data<>(45, "Nachos Cheese"));
        series.getData().add(new XYChart.Data<>(38, "Hot Dog"));
        series.getData().add(new XYChart.Data<>(29, "Combo Mega"));
        series.getData().add(new XYChart.Data<>(23, "Kem que"));
        
        barChart.getData().add(series);
        barChart.setStyle("-fx-background-color: transparent;");
        
        topCombosChartPane.getChildren().clear();
        topCombosChartPane.getChildren().add(barChart);
        
        // Update total revenue
        comboRevenueLabel.setText("Tổng: " + formatCurrency(48630000));
    }
    
    private void loadRevenueByCinemaChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Triệu đồng");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("CGV Vincom", 45.6));
        series.getData().add(new XYChart.Data<>("Lotte Cinema", 38.2));
        series.getData().add(new XYChart.Data<>("Galaxy Cinema", 42.1));
        series.getData().add(new XYChart.Data<>("BHD Star", 28.5));
        series.getData().add(new XYChart.Data<>("Platinum", 31.8));
        
        barChart.getData().add(series);
        barChart.setStyle("-fx-background-color: transparent;");
        
        revenueByCinemaChartPane.getChildren().clear();
        revenueByCinemaChartPane.getChildren().add(barChart);
    }
    
    private void loadRatingBreakdownChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng đánh giá");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("5★", 450));
        series.getData().add(new XYChart.Data<>("4★", 320));
        series.getData().add(new XYChart.Data<>("3★", 180));
        series.getData().add(new XYChart.Data<>("2★", 75));
        series.getData().add(new XYChart.Data<>("1★", 45));
        
        barChart.getData().add(series);
        barChart.setStyle("-fx-background-color: transparent;");
        
        ratingBreakdownChartPane.getChildren().clear();
        ratingBreakdownChartPane.getChildren().add(barChart);
    }
    
    private void loadRecentBookings() {
        ObservableList<BookingRow> bookings = FXCollections.observableArrayList();
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM");
        
        bookings.add(new BookingRow("BK001", "Avatar 3", "CGV Vincom", "P1", 
            "Nguyễn Văn A", "A1, A2", LocalDateTime.now().minusHours(1).format(timeFormatter),
            formatCurrency(350000), "Đã thanh toán"));
        bookings.add(new BookingRow("BK002", "Inception 2", "Lotte Cinema", "P3", 
            "Trần Thị B", "C5-C7", LocalDateTime.now().minusHours(2).format(timeFormatter),
            formatCurrency(525000), "Chờ thanh toán"));
        bookings.add(new BookingRow("BK003", "The Matrix 5", "Galaxy Cinema", "P2", 
            "Lê Văn C", "E10", LocalDateTime.now().minusHours(3).format(timeFormatter),
            formatCurrency(175000), "Đã thanh toán"));
        bookings.add(new BookingRow("BK004", "Dune Part 3", "BHD Star", "P4", 
            "Phạm Thị D", "B3, B4", LocalDateTime.now().minusHours(4).format(timeFormatter),
            formatCurrency(400000), "Đã thanh toán"));
        bookings.add(new BookingRow("BK005", "Spider-Man 4", "CGV Vincom", "P2", 
            "Hoàng Văn E", "D8, D9", LocalDateTime.now().minusHours(5).format(timeFormatter),
            formatCurrency(350000), "Hết hạn"));
        bookings.add(new BookingRow("BK006", "Interstellar 2", "Platinum", "P1", 
            "Võ Thị F", "F5-F8", LocalDateTime.now().minusHours(6).format(timeFormatter),
            formatCurrency(700000), "Đã thanh toán"));
        bookings.add(new BookingRow("BK007", "Oppenheimer 2", "Lotte Cinema", "P5", 
            "Đặng Văn G", "G10", LocalDateTime.now().minusHours(7).format(timeFormatter),
            formatCurrency(175000), "Đã thanh toán"));
        bookings.add(new BookingRow("BK008", "The Batman 2", "Galaxy Cinema", "P1", 
            "Bùi Thị H", "A15-A16", LocalDateTime.now().minusHours(8).format(timeFormatter),
            formatCurrency(380000), "Chờ thanh toán"));
        
        recentBookingsTable.setItems(bookings);
        recentBookingsCountLabel.setText(bookings.size() + " đặt vé hôm nay");
    }
    
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount) + "đ";
    }
    
    // Inner class for table row
    public static class BookingRow {
        private String bookingId;
        private String movieTitle;
        private String cinemaName;
        private String screenName;
        private String customerName;
        private String seats;
        private String showtime;
        private String price;
        private String status;
        
        public BookingRow(String bookingId, String movieTitle, String cinemaName, 
                         String screenName, String customerName, String seats, 
                         String showtime, String price, String status) {
            this.bookingId = bookingId;
            this.movieTitle = movieTitle;
            this.cinemaName = cinemaName;
            this.screenName = screenName;
            this.customerName = customerName;
            this.seats = seats;
            this.showtime = showtime;
            this.price = price;
            this.status = status;
        }
        
        public String getBookingId() { return bookingId; }
        public String getMovieTitle() { return movieTitle; }
        public String getCinemaName() { return cinemaName; }
        public String getScreenName() { return screenName; }
        public String getCustomerName() { return customerName; }
        public String getSeats() { return seats; }
        public String getShowtime() { return showtime; }
        public String getPrice() { return price; }
        public String getStatus() { return status; }
    }
}