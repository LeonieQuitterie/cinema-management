const express = require('express');
const router = express.Router();
const CinemaController = require('../../controllers/admin/cinemaController');

// GET /api/admin/cinemas - List all cinemas
router.get('/', CinemaController.getAllCinemas);

// GET /api/admin/cinemas/:id - Get single cinema
router.get('/:id', CinemaController.getCinemaById);

// POST /api/admin/cinemas - Create new cinema
router.post('/', CinemaController.createCinema);

// PUT /api/admin/cinemas/:id - Update cinema
router.put('/:id', CinemaController.updateCinema);

// DELETE /api/admin/cinemas/:id - Delete cinema
router.delete('/:id', CinemaController.deleteCinema);

module.exports = router;