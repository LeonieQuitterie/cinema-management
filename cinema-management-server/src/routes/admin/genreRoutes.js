const express = require('express');
const router = express.Router();
const db = require('../../config/database');

// GET /api/genres
router.get('/', async (req, res) => {
    try {
        const [genres] = await db.query('SELECT id, name FROM genres ORDER BY name');
        
        res.json({
            success: true,
            data: genres
        });
    } catch (error) {
        console.error('Error fetching genres:', error);
        res.status(500).json({
            success: false,
            error: {
                code: 'DATABASE_ERROR',
                message: 'Không thể tải danh sách thể loại'
            }
        });
    }
});

module.exports = router;