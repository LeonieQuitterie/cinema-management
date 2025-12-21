// src/routes/cinemaRoutes.js
const express = require("express");
const router = express.Router();
const CinemaController = require("../controllers/cinemaController");

/**
 * @route   GET /api/cinemas
 * @desc    Lấy danh sách rạp (có thể filter theo city, cinemaId)
 * @query   city, cinemaId, includeSeats
 * @access  Public
 */
router.get("/", CinemaController.getCinemas);

/**
 * @route   GET /api/cinemas/city/:city
 * @desc    Lấy danh sách rạp theo thành phố (lightweight)
 * @access  Public
 */
router.get("/city/:city", CinemaController.getCinemasByCity);

/**
 * @route   GET /api/cinemas/:id
 * @desc    Lấy chi tiết 1 rạp
 * @access  Public
 */
router.get("/:id", CinemaController.getCinemaById);

module.exports = router;