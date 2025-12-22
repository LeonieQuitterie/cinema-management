// src/services/bookingConfirmService.js

const db = require('../config/database');

class BookingConfirmService {
  static async getMovieDetails(movieId) {
    // Giữ nguyên SELECT * vì MovieDTO có đầy đủ field
    const [rows] = await db.query('SELECT * FROM movies WHERE id = ?', [movieId]);
    return rows[0] || null;
  }

  static async getCinemaDetails(cinemaId) {
    // ✅ CHỈ LẤY ĐÚNG CÁC FIELD CŨ MÀ CLIENT BIẾT
    const query = `
      SELECT 
        id,
        name,
        address,
        city,
        logo_url,
        created_at
      FROM cinemas 
      WHERE id = ?
    `;
    const [rows] = await db.query(query, [cinemaId]);
    return rows[0] || null;
  }

  static async getScreenDetails(screenId) {
    // Chỉ lấy các field cần thiết cho screen + cinema info cơ bản
    const query = `
      SELECT 
        s.id,
        s.name,
        s.cinema_id,
        s.row_count,
        s.column_count,
        s.total_seats,
        c.name AS cinema_name,
        c.address AS cinema_address,
        c.city AS cinema_city,
        c.logo_url AS cinema_logo_url
      FROM screens s
      JOIN cinemas c ON s.cinema_id = c.id
      WHERE s.id = ?
    `;
    const [rows] = await db.query(query, [screenId]);
    return rows[0] || null;
  }
}

module.exports = BookingConfirmService;