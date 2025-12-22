const db = require('../../config/database');
const { v4: uuidv4 } = require('uuid');

class CinemaController {
    /**
     * GET /api/admin/cinemas - Get all cinemas with screen count
     */
    static async getAllCinemas(req, res) {
        try {
            const [cinemas] = await db.query(`
                SELECT 
                    c.id,
                    c.name,
                    c.address,
                    c.city,
                    c.logo_url as logoUrl,
                    COUNT(s.id) as screenCount
                FROM cinemas c
                LEFT JOIN screens s ON c.id = s.cinema_id
                GROUP BY c.id, c.name, c.address, c.city, c.logo_url
                ORDER BY c.name
            `);

            res.json({
                success: true,
                data: cinemas
            });

        } catch (error) {
            console.error('Error in getAllCinemas:', error);
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
     * GET /api/admin/cinemas/:id - Get single cinema with screens
     */
    static async getCinemaById(req, res) {
        try {
            const { id } = req.params;

            const [cinemas] = await db.query(`
                SELECT id, name, address, city, logo_url as logoUrl
                FROM cinemas
                WHERE id = ?
            `, [id]);

            if (cinemas.length === 0) {
                return res.status(404).json({
                    success: false,
                    error: {
                        code: 'NOT_FOUND',
                        message: 'Không tìm thấy rạp'
                    }
                });
            }

            const cinema = cinemas[0];

            // Get screens for this cinema
            const [screens] = await db.query(`
                SELECT 
                    id,
                    name,
                    cinema_id as cinemaId,
                    row_count as rowCount,
                    column_count as columnCount,
                    total_seats as totalSeats
                FROM screens
                WHERE cinema_id = ?
                ORDER BY name
            `, [id]);

            cinema.screens = screens;

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
     * POST /api/admin/cinemas - Create new cinema
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
     * PUT /api/admin/cinemas/:id - Update cinema
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
     * DELETE /api/admin/cinemas/:id - Delete cinema
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
}

module.exports = CinemaController;