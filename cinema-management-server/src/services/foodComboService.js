// src/services/foodComboService.js

const db = require("../config/database");

const getCombosByCinemaId = async (cinemaId) => {
  try {
    const query = `
      SELECT 
        id,
        name,
        description,
        price,
        image_url,
        category,
        available,
        created_at
      FROM food_combos
      WHERE cinema_id = ? OR cinema_id IS NULL
      AND available = TRUE
      ORDER BY created_at DESC
    `;

    const [rows] = await db.query(query, [cinemaId]);
    return rows;
  } catch (error) {
    console.error("Error in getCombosByCinemaId:", error);
    throw new Error("Không thể lấy danh sách combo");
  }
};

module.exports = {
  getCombosByCinemaId,
};