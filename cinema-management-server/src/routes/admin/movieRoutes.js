const express = require('express');
const router = express.Router();
const movieController = require('../../controllers/admin/movieController.js');

// GET /api/admin/movies/stats/summary - Must be before /:id route
router.get('/stats/summary', movieController.getMovieStats.bind(movieController));

// GET /api/admin/movies/search
router.get('/search', movieController.searchMovies.bind(movieController));

// GET /api/admin/movies
router.get('/', movieController.getAllMovies.bind(movieController));

// GET /api/admin/movies/:id
router.get('/:id', movieController.getMovieById.bind(movieController));

// POST /api/admin/movies
router.post('/', movieController.createMovie.bind(movieController));

// PUT /api/admin/movies/:id
router.put('/:id', movieController.updateMovie.bind(movieController));

// DELETE /api/admin/movies/:id
router.delete('/:id', movieController.deleteMovie.bind(movieController));

module.exports = router;