// src/controllers/movieController.js
const MovieService = require('../services/movieService');

class MovieController {
    static async getMovies(req, res) {
        try {
            const { status } = req.query; // ?status=NOW_SHOWING hoặc COMING_SOON

            const movies = await MovieService.getAllMovies(status);

            return res.status(200).json({
                success: true,
                message: "Lấy danh sách phim thành công",
                data: movies
            });
        } catch (error) {
            console.error('Error in getMovies:', error);
            return res.status(500).json({
                success: false,
                message: "Không thể tải danh sách phim"
            });
        }
    }

    // Có thể thêm getMovieDetail(req, res) sau
    // API mới: Lấy thống kê đánh giá sao cho 1 phim
    static async getRatingStats(req, res) {
        try {
            const { id } = req.params;

            if (!id) {
                return res.status(400).json({
                    success: false,
                    message: "Thiếu ID phim"
                });
            }

            const stats = await MovieService.getRatingStats(id);

            return res.status(200).json({
                success: true,
                message: "Lấy thống kê đánh giá thành công",
                data: {
                    average_rating: stats.average_rating,
                    percentages: [
                        stats.percentages.five,
                        stats.percentages.four,
                        stats.percentages.three,
                        stats.percentages.two,
                        stats.percentages.one
                    ],
                    counts: [
                        stats.counts.five,
                        stats.counts.four,
                        stats.counts.three,
                        stats.counts.two,
                        stats.counts.one
                    ],
                    total_ratings: stats.total_ratings
                }
            });
        } catch (error) {
            console.error('Error getting rating stats:', error);
            return res.status(404).json({
                success: false,
                message: error.message || "Không tìm thấy phim"
            });
        }
    }


    static async getMovieCast(req, res) {
        try {
            const { id: movieId } = req.params;

            if (!movieId) {
                return res.status(400).json({
                    success: false,
                    message: "movieId là bắt buộc"
                });
            }

            const cast = await MovieService.getCastByMovieId(movieId);

            return res.status(200).json({
                success: true,
                message: "Lấy danh sách diễn viên thành công",
                data: cast
            });
        } catch (error) {
            console.error('Error in getMovieCast:', error);
            return res.status(500).json({
                success: false,
                message: "Lỗi server khi lấy danh sách diễn viên",
                error: error.message
            });
        }
    }



     // GET /api/movies/:id/comments
    static async getMovieComments(req, res) {
        try {
            const movieId = req.params.id;

            const comments = await MovieService.getMovieComments(movieId);

            return res.json(comments);
        } catch (error) {
            console.error("getMovieComments error:", error);
            return res.status(500).json({
                message: "Lỗi khi lấy bình luận phim"
            });
        }
    }


    
}

module.exports = MovieController;