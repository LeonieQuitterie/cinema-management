const express = require('express');
const cors = require('cors');
require('dotenv').config();

require('./config/database');  // ← Thêm dòng này

const app = express();

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

// Comments (gắn luôn vào movies)
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

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});