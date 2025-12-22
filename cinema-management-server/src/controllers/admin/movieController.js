const movieService = require('../../services/admin/movieService');

class MovieController {
    /**
     * GET /api/movies
     * Get all movies
     */
    async getAllMovies(req, res) {
        try {
            const movies = await movieService.getAllMovies();

            res.json({
                success: true,
                data: movies
            });

        } catch (error) {
            console.error('Error in getAllMovies:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải danh sách phim',
                    details: process.env.NODE_ENV === 'development' ? error.message : undefined
                }
            });
        }
    }

    /**
     * GET /api/movies/:id
     * Get movie by ID
     */
    async getMovieById(req, res) {
        try {
            const { id } = req.params;

            if (!id) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'INVALID_ID',
                        message: 'ID phim không hợp lệ'
                    }
                });
            }

            const movie = await movieService.getMovieById(id);

            if (!movie) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy phim'
                    }
                });
            }

            res.json({
                success: true,
                data: movie
            });

        } catch (error) {
            console.error('Error in getMovieById:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải thông tin phim'
                }
            });
        }
    }

    /**
     * POST /api/movies
     * Create new movie
     */
    async createMovie(req, res) {
        try {
            const movieData = req.body;

            // Validation
            if (!movieData.title || !movieData.duration) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Thiếu thông tin bắt buộc',
                        details: [
                            { field: 'title', message: 'Tên phim là bắt buộc' },
                            { field: 'duration', message: 'Thời lượng là bắt buộc' }
                        ]
                    }
                });
            }

            if (movieData.duration <= 0 || movieData.duration > 500) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Thời lượng phim không hợp lệ (1-500 phút)'
                    }
                });
            }

            const movie = await movieService.createMovie(movieData);

            res.status(201).json({
                success: true,
                data: movie,
                message: 'Tạo phim thành công'
            });

        } catch (error) {
            console.error('Error in createMovie:', error);

            // Handle duplicate entry
            if (error.code === 'ER_DUP_ENTRY') {
                return res.status(409).json({
                    success: false,
                    error: {
                        code: 'DUPLICATE_ENTRY',
                        message: 'Phim đã tồn tại'
                    }
                });
            }

            // Handle foreign key constraint
            if (error.code === 'ER_NO_REFERENCED_ROW_2') {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'INVALID_GENRE',
                        message: 'Thể loại không tồn tại'
                    }
                });
            }

            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tạo phim'
                }
            });
        }
    }

    /**
     * PUT /api/movies/:id
     * Update movie
     */
    async updateMovie(req, res) {
        try {
            const { id } = req.params;
            const movieData = req.body;

            if (!id) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'INVALID_ID',
                        message: 'ID phim không hợp lệ'
                    }
                });
            }

            // Validation
            if (movieData.duration && (movieData.duration <= 0 || movieData.duration > 500)) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Thời lượng phim không hợp lệ (1-500 phút)'
                    }
                });
            }

            const movie = await movieService.updateMovie(id, movieData);

            if (!movie) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy phim'
                    }
                });
            }

            res.json({
                success: true,
                data: movie,
                message: 'Cập nhật phim thành công'
            });

        } catch (error) {
            console.error('Error in updateMovie:', error);

            if (error.code === 'ER_NO_REFERENCED_ROW_2') {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'INVALID_GENRE',
                        message: 'Thể loại không tồn tại'
                    }
                });
            }

            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể cập nhật phim'
                }
            });
        }
    }

    /**
     * DELETE /api/movies/:id
     * Delete movie
     */
    async deleteMovie(req, res) {
        try {
            const { id } = req.params;

            if (!id) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'INVALID_ID',
                        message: 'ID phim không hợp lệ'
                    }
                });
            }

            const deleted = await movieService.deleteMovie(id);

            if (!deleted) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy phim'
                    }
                });
            }

            res.json({
                success: true,
                message: 'Xóa phim thành công'
            });

        } catch (error) {
            console.error('Error in deleteMovie:', error);

            // Handle foreign key constraint (movie has bookings/showtimes)
            if (error.code === 'ER_ROW_IS_REFERENCED_2') {
                return res.status(409).json({
                    success: false,
                    error: {
                        code: 'CONSTRAINT_VIOLATION',
                        message: 'Không thể xóa phim vì có dữ liệu liên quan (lịch chiếu, đặt vé...)'
                    }
                });
            }

            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể xóa phim'
                }
            });
        }
    }

    /**
     * GET /api/movies/search
     * Search movies with filters
     */
    async searchMovies(req, res) {
        try {
            const filters = {
                q: req.query.q,
                status: req.query.status,
                ageRating: req.query.ageRating,
                genreName: req.query.genreName,
                page: parseInt(req.query.page) || 1,
                limit: parseInt(req.query.limit) || 10,
                sortBy: req.query.sortBy || 'release_date',
                sortOrder: req.query.sortOrder || 'DESC'
            };

            // Validation
            if (filters.page < 1) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Số trang phải lớn hơn 0'
                    }
                });
            }

            if (filters.limit < 1 || filters.limit > 100) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Limit phải trong khoảng 1-100'
                    }
                });
            }

            const result = await movieService.searchMovies(filters);

            res.json({
                success: true,
                ...result
            });

        } catch (error) {
            console.error('Error in searchMovies:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tìm kiếm phim'
                }
            });
        }
    }

    /**
     * GET /api/movies/stats/summary
     * Get movie statistics
     */
    async getMovieStats(req, res) {
        try {
            const stats = await movieService.getMovieStats();

            res.json({
                success: true,
                data: stats
            });

        } catch (error) {
            console.error('Error in getMovieStats:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải thống kê'
                }
            });
        }
    }
}

module.exports = new MovieController();