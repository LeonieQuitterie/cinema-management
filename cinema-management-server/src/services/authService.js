// src/services/authService.js
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { v4: uuidv4 } = require('uuid');
const db = require("../config/database");
const JWT_SECRET = process.env.JWT_SECRET || "your-super-secret-jwt-key";

class AuthService {
  // === THAY ĐỔI: Tìm user theo EMAIL thay vì username ===
  static async findUserByEmail(email) {
    const [rows] = await db.query(
      "SELECT * FROM users WHERE email = ? AND is_active = TRUE",
      [email]
    );
    return rows[0] || null;
  }

  // So sánh mật khẩu
  static async validatePassword(inputPassword, hashedPassword) {
    return await bcrypt.compare(inputPassword, hashedPassword);
  }

  // Tạo JWT token
  static generateToken(user) {
    return jwt.sign(
      {
        id: user.id,
        username: user.username,
        role: user.role,
        email: user.email,
      },
      JWT_SECRET,
      { expiresIn: "7d" }
    );
  }

  // Cập nhật last_login
  static async updateLastLogin(userId) {
    await db.query(
      "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?",
      [userId]
    );
  }

  // === HÀM LOGIN MỚI: Nhận email thay vì username ===
  static async login(email, password) {
    const user = await this.findUserByEmail(email);
    if (!user) {
      throw new Error("Email hoặc mật khẩu không đúng");
    }

    const isValid = await this.validatePassword(password, user.password_hash);
    if (!isValid) {
      throw new Error("Email hoặc mật khẩu không đúng");
    }

    await this.updateLastLogin(user.id);

    const token = this.generateToken(user);

    return {
      token,
      user: {
        id: user.id,
        username: user.username,
        full_name: user.full_name,
        email: user.email,
        role: user.role,
        avatar_url: user.avatar_url,
      },
    };
  }

  // Kiểm tra trùng username hoặc email khi đăng ký (giữ nguyên)
  static async checkExistingUser(username, email) {
    const [rows] = await db.query(
      'SELECT username, email FROM users WHERE username = ? OR email = ?',
      [username, email]
    );
    return rows;
  }

  // Đăng ký giữ nguyên (vẫn cần username vì bảng CSDL yêu cầu)
  static async register(full_name, username, email, password, confirmPassword) {
    if (!full_name || !username || !email || !password || !confirmPassword) {
      throw new Error('Vui lòng điền đầy đủ thông tin');
    }

    if (password !== confirmPassword) {
      throw new Error('Mật khẩu nhập lại không khớp');
    }

    if (password.length < 6) {
      throw new Error('Mật khẩu phải có ít nhất 6 ký tự');
    }

    const existing = await this.checkExistingUser(username, email);
    if (existing.length > 0) {
      for (const row of existing) {
        if (row.username === username) {
          throw new Error('Tên đăng nhập đã được sử dụng');
        }
        if (row.email === email) {
          throw new Error('Email đã được sử dụng');
        }
      }
    }

    const password_hash = await bcrypt.hash(password, 10);
    const userId = 'usr_' + uuidv4().split('-')[0];

    try {
      await db.query(
        `INSERT INTO users 
         (id, username, password_hash, email, full_name, role, is_active)
         VALUES (?, ?, ?, ?, ?, 'CUSTOMER', TRUE)`,
        [userId, username.trim(), password_hash, email.trim(), full_name.trim()]
      );
    } catch (err) {
      if (err.code === 'ER_DUP_ENTRY') {
        throw new Error('Tên đăng nhập hoặc email đã tồn tại');
      }
      throw new Error('Đăng ký thất bại, vui lòng thử lại');
    }

    const token = jwt.sign(
      { id: userId, username, email, role: 'CUSTOMER' },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    return {
      token,
      user: {
        id: userId,
        username,
        full_name: full_name.trim(),
        email: email.trim(),
        role: 'CUSTOMER',
        avatar_url: null,
      },
    };
  }
}

module.exports = AuthService;