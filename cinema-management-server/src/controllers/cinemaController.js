const CinemaService = require('../services/cinemaService');
const db = require('../config/database');
const { v4: uuidv4 } = require('uuid');

class CinemaController {
    /**
     * GET /api/cinemas
     */
    static async getCinemas(req, res) {
        try {
            const { city, cinemaId } = req.query;
            
            const cinemas = await CinemaService.getCinemasWithScreens({
                city,
                cinemaId
            });
            
            res.json({
                success: true,
                data: cinemas
            });
            
        } catch (error) {
            console.error('Error in getCinemas:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải danh sách rạp'
                }
            });
        }
    }

    /**
     * GET /api/cinemas/:id
     */
    static async getCinemaById(req, res) {
        try {
            const { id } = req.params;
            
            const cinema = await CinemaService.getCinemaById(id);
            
            if (!cinema) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy rạp'
                    }
                });
            }
            
            res.json({
                success: true,
                data: cinema
            });
            
        } catch (error) {
            console.error('Error in getCinemaById:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải thông tin rạp'
                }
            });
        }
    }

    /**
     * GET /api/cinemas/city/:city
     */
    static async getCinemasByCity(req, res) {
        try {
            const { city } = req.params;
            
            const cinemas = await CinemaService.getCinemasByCity(city);
            
            res.json({
                success: true,
                data: cinemas
            });
            
        } catch (error) {
            console.error('Error in getCinemasByCity:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tải danh sách rạp'
                }
            });
        }
    }

    /**
     * POST /api/cinemas - Create new cinema
     */
    static async createCinema(req, res) {
        try {
            const { name, address, city, logoUrl } = req.body;

            // Validation
            if (!name || !address || !city) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Thiếu thông tin bắt buộc'
                    }
                });
            }

            const id = uuidv4();

            await db.query(`
                INSERT INTO cinemas (id, name, address, city, logo_url)
                VALUES (?, ?, ?, ?, ?)
            `, [id, name, address, city, logoUrl || null]);

            res.status(201).json({
                success: true,
                data: {
                    id,
                    name,
                    address,
                    city,
                    logoUrl: logoUrl || null
                }
            });

        } catch (error) {
            console.error('Error in createCinema:', error);
            
            if (error.code === 'ER_DUP_ENTRY') {
                return res.status(409).json({
                    success: false,
                    error: {
                        code: 'DUPLICATE_ENTRY',
                        message: 'Rạp đã tồn tại'
                    }
                });
            }

            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể tạo rạp mới'
                }
            });
        }
    }

    /**
     * PUT /api/cinemas/:id - Update cinema
     */
    static async updateCinema(req, res) {
        try {
            const { id } = req.params;
            const { name, address, city, logoUrl } = req.body;

            // Check if cinema exists
            const [existing] = await db.query(
                'SELECT id FROM cinemas WHERE id = ?',
                [id]
            );

            if (existing.length === 0) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy rạp'
                    }
                });
            }

            // Build dynamic update query
            const updates = [];
            const values = [];

            if (name !== undefined) {
                updates.push('name = ?');
                values.push(name);
            }
            if (address !== undefined) {
                updates.push('address = ?');
                values.push(address);
            }
            if (city !== undefined) {
                updates.push('city = ?');
                values.push(city);
            }
            if (logoUrl !== undefined) {
                updates.push('logo_url = ?');
                values.push(logoUrl);
            }

            if (updates.length === 0) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'NO_UPDATES',
                        message: 'Không có thông tin cần cập nhật'
                    }
                });
            }

            values.push(id);

            await db.query(
                `UPDATE cinemas SET ${updates.join(', ')} WHERE id = ?`,
                values
            );

            // Get updated cinema
            const [updated] = await db.query(
                'SELECT id, name, address, city, logo_url as logoUrl FROM cinemas WHERE id = ?',
                [id]
            );

            res.json({
                success: true,
                data: updated[0]
            });

        } catch (error) {
            console.error('Error in updateCinema:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể cập nhật rạp'
                }
            });
        }
    }

    /**
     * DELETE /api/cinemas/:id - Delete cinema
     */
    static async deleteCinema(req, res) {
        try {
            const { id } = req.params;

            // Check if cinema exists
            const [existing] = await db.query(
                'SELECT id FROM cinemas WHERE id = ?',
                [id]
            );

            if (existing.length === 0) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy rạp'
                    }
                });
            }

            // Check if cinema has screens
            const [screens] = await db.query(
                'SELECT COUNT(*) as count FROM screens WHERE cinema_id = ?',
                [id]
            );

            if (screens[0].count > 0) {
                return res.status(409).json({
                    success: false,
                    error: {
                        code: 'HAS_SCREENS',
                        message: 'Không thể xóa rạp có phòng chiếu'
                    }
                });
            }

            await db.query('DELETE FROM cinemas WHERE id = ?', [id]);

            res.json({
                success: true,
                message: 'Đã xóa rạp thành công'
            });

        } catch (error) {
            console.error('Error in deleteCinema:', error);

            if (error.code === 'ER_ROW_IS_REFERENCED_2') {
                return res.status(409).json({
                    success: false,
                    error: {
                        code: 'FOREIGN_KEY_CONSTRAINT',
                        message: 'Không thể xóa rạp có dữ liệu liên quan'
                    }
                });
            }

            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể xóa rạp'
                }
            });
        }
    }

    /**
     * POST /api/cinemas/:cinemaId/screens/bulk-seat-layout
     * Apply seat layout to all screens in a cinema
     */
    static async updateBulkSeatLayout(req, res) {
        const connection = await db.getConnection();
        
        try {
            const { cinemaId } = req.params;
            const { rowCount, columnCount, seats } = req.body;

            // Validation
            if (!rowCount || !columnCount || !seats) {
                return res.status(400).json({
                    success: false,
                    error: {
                        code: 'VALIDATION_ERROR',
                        message: 'Thiếu thông tin sơ đồ ghế'
                    }
                });
            }

            // Check cinema exists
            const [cinema] = await connection.query(
                'SELECT id FROM cinemas WHERE id = ?',
                [cinemaId]
            );

            if (cinema.length === 0) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy rạp'
                    }
                });
            }

            // Get all screens for this cinema
            const [screens] = await connection.query(
                'SELECT id FROM screens WHERE cinema_id = ?',
                [cinemaId]
            );

            if (screens.length === 0) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NO_SCREENS',
                        message: 'Rạp chưa có phòng chiếu'
                    }
                });
            }

            await connection.beginTransaction();

            let totalSeatsUpdated = 0;

            // For each screen
            for (const screen of screens) {
                const screenId = screen.id;

                // Delete old seats
                await connection.query('DELETE FROM seats WHERE screen_id = ?', [screenId]);

                // Update screen dimensions
                const totalSeatsCount = seats.filter(s => s !== null).length;
                await connection.query(
                    `UPDATE screens 
                    SET row_count = ?, column_count = ?, total_seats = ?
                    WHERE id = ?`,
                    [rowCount, columnCount, totalSeatsCount, screenId]
                );

                // Insert new seats
                for (let rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    for (let colIndex = 0; colIndex < columnCount; colIndex++) {
                        const seat = seats[rowIndex * columnCount + colIndex];
                        
                        if (seat) {
                            const seatId = `${screenId}_${seat.seatNumber}`;
                            
                            await connection.query(
                                `INSERT INTO seats 
                                (id, screen_id, seat_number, seat_type, price, row_index, col_index)
                                VALUES (?, ?, ?, ?, ?, ?, ?)`,
                                [
                                    seatId,
                                    screenId,
                                    seat.seatNumber,
                                    seat.seatType,
                                    seat.price,
                                    rowIndex,
                                    colIndex
                                ]
                            );
                            
                            totalSeatsUpdated++;
                        }
                    }
                }
            }

            await connection.commit();

            res.json({
                success: true,
                data: {
                    cinemaId,
                    screensUpdated: screens.length,
                    totalSeatsCreated: totalSeatsUpdated
                }
            });

        } catch (error) {
            await connection.rollback();
            console.error('Error in updateBulkSeatLayout:', error);
            res.status(500).json({
                success: false,
                error: {
                    code: 'DATABASE_ERROR',
                    message: 'Không thể cập nhật sơ đồ ghế'
                }
            });
        } finally {
            connection.release();
        }
    }
    /**
     * GET /api/cinemas/movie/:movieId
     * Lấy danh sách rạp đang chiếu phim (từ hôm nay)
     */
    static async getCinemasByMovie(req, res) {
        try {
            const { movieId } = req.params;
            const isAdmin = req.query.isAdmin === 'true';
            
            const cinemas = await CinemaService.getCinemasByMovieId(movieId, isAdmin);

            res.json({
                success: true,
                data: cinemas,
                count: cinemas.length
            });

            } catch (error) {
            console.error("Error in getCinemasByMovie:", error);
            res.status(500).json({
                success: false,
                message: error.message || "Lỗi khi lấy danh sách rạp",
                error: error.message
            });
        }
    }
}

module.exports = CinemaController;