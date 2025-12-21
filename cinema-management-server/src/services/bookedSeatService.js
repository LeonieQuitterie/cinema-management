// src/services/bookedSeatService.js

const db = require('../config/database'); // điều chỉnh đường dẫn nếu cần

class BookedSeatService {
  static async getBookedSeatsByShowtime(showtimeId) {
    const query = `
      SELECT DISTINCT bs.seat_number
      FROM booking_seats bs
      INNER JOIN bookings b ON bs.booking_id = b.id
      WHERE b.showtime_id = ?
        AND b.payment_status = 'PAID'
      ORDER BY bs.seat_number
    `;

    const [rows] = await db.query(query, [showtimeId]);
    return rows.map(row => row.seat_number);
  }

  static async isShowtimeExist(showtimeId) {
    const [rows] = await db.query('SELECT 1 FROM showtimes WHERE id = ?', [showtimeId]);
    return rows.length > 0;
  }
}

module.exports = BookedSeatService;