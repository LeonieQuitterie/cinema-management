// src/controllers/authController.js
const AuthService = require("../services/authService");

class AuthController {
  static async login(req, res) {
    try {
      console.log("HEADERS:", req.headers);
      console.log("BODY:", req.body);

      const { email, password } = req.body;

      if (!email || !password) {
        return res.status(400).json({
          success: false,
          message: "Vui lòng nhập email và mật khẩu",
        });
      }

      const result = await AuthService.login(email, password);

      return res.status(200).json({
        success: true,
        message: "Đăng nhập thành công",
        data: result,
      });
    } catch (error) {
      console.error("LOGIN CONTROLLER ERROR:", error);
      return res.status(500).json({
        success: false,
        message: error.message || "Đăng nhập thất bại",
      });
    }
  }


  // Register giữ nguyên (vẫn cần username)
  static async register(req, res) {
    try {
      const { full_name, username, email, password, confirm_password } = req.body;

      if (!full_name || !username || !email || !password || !confirm_password) {
        return res.status(400).json({
          success: false,
          message: "Vui lòng điền đầy đủ các trường",
        });
      }

      const result = await AuthService.register(
        full_name,
        username,
        email,
        password,
        confirm_password
      );

      return res.status(201).json({
        success: true,
        message: "Đăng ký thành công! Chào mừng bạn đến với Cinema Pro",
        data: result,
      });
    } catch (error) {
      return res.status(400).json({
        success: false,
        message: error.message || "Đăng ký thất bại",
      });
    }
  }
}

module.exports = AuthController;