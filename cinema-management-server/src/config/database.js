// src/config/database.js
const mysql = require('mysql2/promise');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'cinema_management',
    port: process.env.DB_PORT || 3306,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    timezone: '+07:00'
});

// ================== THÊM ĐOẠN NÀY ĐỂ IN THÔNG BÁO ==================
(async () => {
    try {
        const connection = await pool.getConnection();
        console.log('✅ Kết nối database thành công!');
        connection.release(); // trả kết nối về pool
    } catch (err) {
        console.error('❌ Kết nối database thất bại:', err.message);
        // Tùy chọn: thoát server nếu không kết nối được DB (rất hữu ích khi dev)
        // process.exit(1);
    }
})();
// =================================================================

module.exports = pool;