// src/routes/bookingConfirmRouter.js

const express = require('express');
const BookingConfirmController = require('../controllers/bookingConfirmController');

const router = express.Router();

// API 1: Lấy chi tiết phim theo movieId
router.get('/movie/:movieId', BookingConfirmController.fetchMovieDetails);

// API 2: Lấy chi tiết rạp chiếu theo cinemaId
router.get('/cinema/:cinemaId', BookingConfirmController.fetchCinemaDetails);

// API 3: Lấy chi tiết màn hình chiếu theo screenId
router.get('/screen/:screenId', BookingConfirmController.fetchScreenDetails);

module.exports = router;