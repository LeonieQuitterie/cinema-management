// src/routes/authRoutes.js
const express = require('express');
const AuthController = require('../controllers/authController');

const router = express.Router();

/**
 * @swagger
 * /api/auth/login:
 *   post:
 *     summary: Đăng nhập hệ thống
 *     tags: [Auth]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               username:
 *                 type: string
 *                 example: admin
 *               password:
 *                 type: string
 *                 format: password
 *                 example: 123456
 *     responses:
 *       200:
 *         description: Đăng nhập thành công
 *       400:
 *         description: Thiếu thông tin
 *       401:
 *         description: Sai tên đăng nhập hoặc mật khẩu
 */
router.post('/login', AuthController.login);
router.post('/register', AuthController.register);

module.exports = router;