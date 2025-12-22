// src/services/customerService.js

const db = require('../config/database');

class CustomerService {
  static async getCustomerProfile(userId) {
    const query = `
      SELECT 
        id,
        full_name AS fullName,
        phone_number AS phoneNumber,
        email
      FROM users 
      WHERE id = ? 
        AND role = 'CUSTOMER' 
        AND is_active = TRUE
    `;

    const [rows] = await db.query(query, [userId]);

    if (rows.length === 0) {
      return null;
    }

    const user = rows[0];

    return {
      id: user.id,                    // lấy nguyên id từ database
      fullName: user.fullName || '',
      phoneNumber: user.phoneNumber || '',
      email: user.email || ''
    };
  }
}

module.exports = CustomerService;