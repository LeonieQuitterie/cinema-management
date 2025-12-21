// src/routes/cinemaRoutes.js
const express = require("express");
const router = express.Router();
const CinemaController = require("../controllers/cinemaController");

/**
 * @route   GET /api/cinemas/movie/:movieId
 * @desc    Lấy tất cả rạp đang chiếu phim (từ hôm nay trở đi)
 * @access  Public
 */
router.get("/movie/:movieId", CinemaController.getCinemasByMovie);

module.exports = router;