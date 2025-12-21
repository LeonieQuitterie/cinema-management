// src/routes/movieRoutes.js
const express = require('express');
const MovieController = require('../controllers/movieController');

const router = express.Router();

// GET /api/movies
// Ví dụ: 
// - /api/movies → tất cả phim
// - /api/movies?status=NOW_SHOWING → chỉ phim đang chiếu
// - /api/movies?status=COMING_SOON → phim sắp chiếu
router.get('/', MovieController.getMovies);

// API mới: Thống kê đánh giá sao cho phim
router.get('/:id/rating-stats', MovieController.getRatingStats);

// API mới: Lấy dàn diễn viên của phim
router.get('/:id/cast', MovieController.getMovieCast);


// 🔥 COMMENTS + REACTIONS
router.get('/:id/comments', MovieController.getMovieComments);

module.exports = router;