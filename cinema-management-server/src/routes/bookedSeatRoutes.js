// src/routes/bookedSeatRoutes.js

const express = require('express');
const router = express.Router({ mergeParams: true }); // cần để lấy :showtimeId từ route cha
const BookedSeatController = require('../controllers/bookedSeatController');

// GET /booked-seats  → sẽ được mount dưới /api/showtimes/:showtimeId
router.get('/booked-seats', BookedSeatController.getBookedSeats);

module.exports = router;