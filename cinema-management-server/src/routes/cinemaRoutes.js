// src/routes/cinemaRoutes.js
const express = require("express");
const router = express.Router();
const CinemaController = require("../controllers/cinemaController");

router.get("/", CinemaController.getCinemas);

/**
 * @route   GET /api/cinemas/movie/:movieId
 * @desc    Lấy tất cả rạp đang chiếu phim (từ hôm nay trở đi)
 * @access  Public
 */
router.get("/movie/:movieId", CinemaController.getCinemasByMovie);

/**
 * @route   POST /api/cinemas
 * @desc    Create new cinema
 * @access  Admin
 */
router.post('/', CinemaController.createCinema);

/**
 * @route   PUT /api/cinemas/:id
 * @desc    Update cinema
 * @access  Admin
 */
router.put('/:id', CinemaController.updateCinema);

/**
 * @route   DELETE /api/cinemas/:id
 * @desc    Delete cinema
 * @access  Admin
 */
router.delete('/:id', CinemaController.deleteCinema);

/**
 * @route   POST /api/cinemas/:cinemaId/screens/bulk-seat-layout
 * @desc    Update seat layout for all screens in cinema
 * @access  Admin
 */
router.post('/:cinemaId/screens/bulk-seat-layout', CinemaController.updateBulkSeatLayout);

module.exports = router;