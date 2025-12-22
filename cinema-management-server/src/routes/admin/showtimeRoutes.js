const express = require('express');
const router = express.Router();
const showtimeController = require('../../controllers/admin/showtimeController');

// GET /api/showtimes/stats/summary - Must be before /:id
router.get('/stats/summary', showtimeController.getShowtimeStats.bind(showtimeController));

// POST /api/admin/showtimes/bulk - Bulk create
router.post('/bulk', showtimeController.createBulkShowtimes.bind(showtimeController));

// GET /api/showtimes
router.get('/', showtimeController.getAllShowtimes.bind(showtimeController));

// GET /api/showtimes/:id
router.get('/:id', showtimeController.getShowtimeById.bind(showtimeController));

// POST /api/showtimes
router.post('/', showtimeController.createShowtime.bind(showtimeController));

// PUT /api/showtimes/:id
router.put('/:id', showtimeController.updateShowtime.bind(showtimeController));

// DELETE /api/showtimes/:id
router.delete('/:id', showtimeController.deleteShowtime.bind(showtimeController));

module.exports = router;