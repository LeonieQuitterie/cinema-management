const db = require('../config/database');

// helper: undefined → null
const n = (v) => (v === undefined ? null : v);

class BookingService {
  static async createBooking(booking) {

    // ===== DEBUG 1: RAW PAYLOAD =====
    console.log('🟡 [DEBUG] Booking payload received:');
    console.dir(booking, { depth: null });

    const sql = `
      INSERT INTO bookings (
        id, movie_id, cinema_id, screen_id, showtime_id, user_id,
        seat_total_price, combo_total_price, total_price,
        payment_status, booking_time, payment_deadline,
        bank_name, account_holder, account_number,
        transfer_content, qr_code_url
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    // ✅ ĐÚNG: dùng snake_case + user_id
    const params = [
      n(booking.id),
      n(booking.movie_id),
      n(booking.cinema_id),
      n(booking.screen_id),
      n(booking.showtime_id),
      n(booking.customer_id),

      n(booking.seat_total_price),
      n(booking.combo_total_price ?? 0),
      n(booking.total_price),

n(booking.payment_status ?? 'PENDING'),

      n(booking.booking_time),
      n(booking.payment_deadline),

      n(booking.bank_name),
      n(booking.account_holder),
      n(booking.account_number),
      n(booking.transfer_content),
      n(booking.qr_code_url)
    ];

    // ===== DEBUG 2: PARAM CHECK =====
    console.log('🟡 [DEBUG] SQL params:');
    params.forEach((p, i) => {
      console.log(`${i + 1}.`, p === null ? 'NULL ❗' : p);
    });

    try {
      const [result] = await db.execute(sql, params);
      console.log('🟢 [DEBUG] Booking inserted successfully:', result);
      return result;
    } catch (err) {
      console.error('🔴 [ERROR] Insert booking failed');
      console.error('   code:', err.code);
      console.error('   errno:', err.errno);
      console.error('   sqlState:', err.sqlState);
      console.error('   sqlMessage:', err.sqlMessage);
      throw err;
    }
  }






   // ✅ THÊM METHOD MỚI: Lấy booking theo ID
  static async getBookingById(bookingId) {
    const sql = `
      SELECT * FROM bookings 
      WHERE id = ?
    `;
    
    try {
      const [rows] = await db.execute(sql, [bookingId]);
      return rows.length > 0 ? rows[0] : null;
    } catch (err) {
      console.error('🔴 [ERROR] Get booking failed:', err);
      throw err;
    }
  }

  // ✅ THÊM METHOD MỚI: Lấy booking theo PayOS orderCode
  static async getBookingByOrderCode(orderCode) {
    const sql = `
      SELECT * FROM bookings 
      WHERE payos_order_code = ?
    `;
    
    try {
      const [rows] = await db.execute(sql, [orderCode]);
      return rows.length > 0 ? rows[0] : null;
    } catch (err) {
      console.error('🔴 [ERROR] Get booking by orderCode failed:', err);
      throw err;
    }
  }

  // ✅ THÊM METHOD MỚI: Update payment status
  static async updatePaymentStatus(bookingId, paymentData) {
    const sql = `
      UPDATE bookings 
      SET 
        payment_status = ?,
        payment_time = ?,
        payment_transaction_id = ?,
        payos_order_code = ?
      WHERE id = ?
    `;

    const params = [
      paymentData.status || 'PAID',
      paymentData.paymentTime || new Date(),
      n(paymentData.transactionId),
      n(paymentData.orderCode),
      bookingId
    ];

    try {
      const [result] = await db.execute(sql, params);
      console.log('✅ Payment status updated for booking:', bookingId);
      return result;
    } catch (err) {
      console.error('🔴 [ERROR] Update payment status failed:', err);
      throw err;
    }
  }
}

module.exports = BookingService;
