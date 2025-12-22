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

const movieRoutes = require('./routes/movieRoutes');
app.use('/api/movies', movieRoutes);

const commentRoutes = require('./routes/commentRoutes');
app.use('/api/movies', commentRoutes);

const cinemaRoutes = require("./routes/cinemaRoutes");
app.use("/api/cinemas", cinemaRoutes);

const bookedSeatRoutes = require('./routes/bookedSeatRoutes');
app.use('/api/showtimes/:showtimeId', bookedSeatRoutes);

// === SOCKET.IO SETUP ===
const setupSeatSocket = require('./socket/seatSocket');
setupSeatSocket(io);

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
    console.log(`WebSocket available at ws://localhost:${PORT}`);
});