const showtimeService = require('../../services/admin/showtimeService');

class ShowtimeController {
    /**
     * GET /api/showtimes
     */
    async getAllShowtimes(req, res) {
        try {
            const filters = {
                date: req.query.date,
                cinemaId: req.query.cinemaId,
                screenId: req.query.screenId,
                movieId: req.query.movieId
            };
            
            const showtimes = await showtimeService.getAllShowtimes(filters);
            
            res.json({
                success: true,
                data: showtimes
            });
            
        } catch (error) {
            console.error('Error in getAllShowtimes:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải danh sách lịch chiếu'
                }
            });
        }
    }

    /**
     * GET /api/showtimes/:id
     */
    async getShowtimeById(req, res) {
        try {
            const { id } = req.params;
            
            const showtime = await showtimeService.getShowtimeById(id);
            
            if (!showtime) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy lịch chiếu'
                    }
                });
            }
            
            res.json({
                success: true,
                data: showtime
            });
            
        } catch (error) {
            console.error('Error in getShowtimeById:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải thông tin lịch chiếu'
                }
            });
        }
    }

    /**
     * POST /api/showtimes
     */
    async createShowtime(req, res) {
        try {
            const showtimeData = req.body;
            
            // Validation
            if (!showtimeData.movie_id || !showtimeData.screen_id || 
                !showtimeData.start_time || !showtimeData.end_time) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Thiếu thông tin bắt buộc'
                    }
                });
            }
            
            const showtime = await showtimeService.createShowtime(showtimeData);
            
            res.status(201).json({
                success: true,
                data: showtime,
                message: 'Tạo lịch chiếu thành công'
            });
            
        } catch (error) {
            console.error('Error in createShowtime:', error);
            
            if (error.code === 'ER_NO_REFERENCED_ROW_2') {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'INVALID_REFERENCE',
                        message: 'Phim hoặc phòng chiếu không tồn tại'
                    }
                });
            }
            
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tạo lịch chiếu'
                }
            });
        }
    }

    /**
     * POST /api/admin/showtimes/bulk
     */
    async createBulkShowtimes(req, res) {
        try {
            const { showtimes } = req.body;
            
            if (!Array.isArray(showtimes) || showtimes.length === 0) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Danh sách lịch chiếu không hợp lệ'
                    }
                });
            }
            
            const createdIds = await showtimeService.createBulkShowtimes(showtimes);
            
            res.status(201).json({
                success: true,
                data: { ids: createdIds, count: createdIds.length },
                message: `Đã tạo ${createdIds.length} lịch chiếu thành công`
            });
            
        } catch (error) {
            console.error('Error in createBulkShowtimes:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tạo lịch chiếu hàng loạt'
                }
            });
        }
    }

    /**
     * PUT /api/showtimes/:id
     */
    async updateShowtime(req, res) {
        try {
            const { id } = req.params;
            const showtimeData = req.body;
            
            const showtime = await showtimeService.updateShowtime(id, showtimeData);
            
            if (!showtime) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy lịch chiếu'
                    }
                });
            }
            
            res.json({
                success: true,
                data: showtime,
                message: 'Cập nhật lịch chiếu thành công'
            });
            
        } catch (error) {
            console.error('Error in updateShowtime:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể cập nhật lịch chiếu'
                }
            });
        }
    }

    /**
     * DELETE /api/showtimes/:id
     */
    async deleteShowtime(req, res) {
        try {
            const { id } = req.params;
            
            const deleted = await showtimeService.deleteShowtime(id);
            
            if (!deleted) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy lịch chiếu'
                    }
                });
            }
            
            res.json({
                success: true,
                message: 'Xóa lịch chiếu thành công'
            });
            
        } catch (error) {
            console.error('Error in deleteShowtime:', error);
            
            if (error.code === 'ER_ROW_IS_REFERENCED_2') {
                return res.status(409).json({
                    success: false,
                    error: {
                        code: 'CONSTRAINT_VIOLATION',
                        message: 'Không thể xóa lịch chiếu vì có đặt vé liên quan'
                    }
                });
            }
            
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể xóa lịch chiếu'
                }
            });
        }
    }

    /**
     * GET /api/showtimes/stats/summary
     */
    async getShowtimeStats(req, res) {
        try {
            const { date } = req.query;
            
            const stats = await showtimeService.getShowtimeStats(date);
            
            res.json({
                success: true,
                data: stats
            });
            
        } catch (error) {
            console.error('Error in getShowtimeStats:', error);
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

module.exports = new ShowtimeController();