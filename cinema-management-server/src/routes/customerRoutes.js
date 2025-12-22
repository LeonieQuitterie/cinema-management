// src/routes/customerRoutes.js

const express = require('express');
const router = express.Router();
const CustomerController = require('../controllers/customerController');
const authMiddleware = require('../middlewares/authMiddleware');

router.get('/me', authMiddleware, CustomerController.getMyProfile);

module.exports = router;