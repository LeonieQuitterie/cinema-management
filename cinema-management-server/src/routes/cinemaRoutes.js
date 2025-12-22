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