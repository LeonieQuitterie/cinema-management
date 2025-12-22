// src/services/cinemaBankService.js

const db = require('../config/database');

class CinemaBankService {
  static async getBankInfo(cinemaId) {
    const query = `
      SELECT 
        bank_name,
        bank_account_holder,
        bank_account_number,
        bank_branch,
        bank_qr_template
      FROM cinemas
      WHERE id = ?
    `;

    const [rows] = await db.query(query, [cinemaId]);
    return rows[0] || null; // Trả về object hoặc null nếu không tìm thấy
  }
}

module.exports = CinemaBankService;