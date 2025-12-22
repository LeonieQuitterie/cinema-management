// src/controllers/foodComboController.js

const foodComboService = require("../services/foodComboService");

const getCombosByCinema = async (req, res) => {
  try {
    const { cinemaId } = req.params;

    if (!cinemaId) {
      return res.status(400).json({
        success: false,
        message: "Thiếu cinemaId",
      });
    }

    const combos = await foodComboService.getCombosByCinemaId(cinemaId);

    return res.status(200).json({
      success: true,
      message: "Lấy danh sách combo thành công",
      data: combos,
    });
  } catch (error) {
    console.error("Error in getCombosByCinema:", error);
    return res.status(500).json({
      success: false,
      message: error.message || "Lỗi server khi lấy combo",
    });
  }
};

module.exports = {
  getCombosByCinema,
};