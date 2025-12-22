// src/routes/cinemaBankRouter.js

const express = require('express');
const CinemaBankController = require('../controllers/cinemaBankController');

const router = express.Router();

// GET /api/cinemas/:cinemaId/bank-info
router.get('/:cinemaId/bank-info', CinemaBankController.getBankInfo);

module.exports = router;