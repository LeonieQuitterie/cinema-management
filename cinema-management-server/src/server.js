const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
require('dotenv').config();

require('./config/database');

const app = express();
const server = http.createServer(app); // Thay đổi từ app.listen → tạo HTTP server

// Socket.io setup
const io = new Server(server, {
    cors: {
        origin: "*", // Hoặc chỉ định cụ thể: "http://localhost:8080"
        methods: ["GET", "POST"]
    }
});

app.use(cors());
app.use(express.json());

app.get('/api/test', (req, res) => {
    res.json({ message: 'Server running' });
});

const authRoutes = require('./routes/authRoutes');
app.use('/api/auth', authRoutes);

// src/server.js
const movieRoutes = require('./routes/movieRoutes');
app.use('/api/movies', movieRoutes);

const commentRoutes = require('./routes/commentRoutes');
app.use('/api/movies', commentRoutes);

const cinemaRoutes = require("./routes/cinemaRoutes");
app.use("/api/cinemas", cinemaRoutes);

const adminMovieRoutes = require('./routes/admin/movieRoutes');
app.use('/api/admin/movies', adminMovieRoutes);

const adminGenreRoutes = require('./routes/admin/genreRoutes');
app.use('/api/admin/genres', adminGenreRoutes);

const adminShowtimeRoutes = require('./routes/admin/showtimeRoutes');
app.use('/api/admin/showtimes', adminShowtimeRoutes);

const adminCinemaRoutes = require('./routes/admin/cinemaRoutes');
app.use('/api/admin/cinemas', adminCinemaRoutes);

const bookedSeatRoutes = require('./routes/bookedSeatRoutes');
app.use('/api/showtimes/:showtimeId', bookedSeatRoutes);

const foodComboRoutes = require("./routes/foodComboRoutes");
app.use("/api/combos", foodComboRoutes);

const customerRoutes = require('./routes/customerRoutes');
app.use('/api/customers', customerRoutes);


const bookingConfirmRoutes =    require('./routes/bookingConfirmRouter');
app.use('/api/booking-confirm', bookingConfirmRoutes);

const cinemaBankRouter = require('./routes/cinemaBankRouter');
app.use('/api/cinema-bank', cinemaBankRouter);

const bookingRoutes = require('./routes/bookingRoutes');
app.use('/api/bookings', bookingRoutes);


// ✅ THÊM: Payment routes
const { router: paymentRouter, setPaymentNamespace } = require('./routes/paymentRoutes');
app.use('/api/payment', paymentRouter);

// === SOCKET.IO SETUP ===
const setupSeatSocket = require('./socket/seatSocket');
const setupPaymentSocket = require('./socket/paymentSocket'); // ✅ THÊM



// ✅ SỬA THÀNH ASYNC/AWAIT
(async () => {
    await setupSeatSocket(io);
    
    // ✅ THÊM: Setup payment socket
    const paymentNamespace = setupPaymentSocket(io);
    setPaymentNamespace(paymentNamespace); // Inject vào payment routes
    
    const PORT = process.env.PORT || 3000;
    server.listen(PORT, () => {
        console.log(`Server running on port ${PORT}`);
        console.log(`WebSocket available at ws://localhost:${PORT}`);
        console.log(`  - Seats: ws://localhost:${PORT}/seats`);
        console.log(`  - Payment: ws://localhost:${PORT}/payment`); // ✅ THÊM
    });
})();