// src/controllers/bookedSeatController.js

const BookedSeatService = require('../services/bookedSeatService');

class BookedSeatController {
  static async getBookedSeats(req, res) {
    const { showtimeId } = req.params;

    if (!showtimeId) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu showtimeId'
      });
    }

    try {
      const exist = await BookedSeatService.isShowtimeExist(showtimeId);
      if (!exist) {
        return res.status(404).json({
          success: false,
          message: 'Suất chiếu không tồn tại'
        });
      }

      const bookedSeats = await BookedSeatService.getBookedSeatsByShowtime(showtimeId);

      res.json({
        success: true,
        data: bookedSeats
      });
    } catch (error) {
      console.error('Error in BookedSeatController:', error);
      res.status(500).json({
        success: false,
        message: 'Lỗi server khi lấy danh sách ghế đã đặt'
      });
    }
  }
}

module.exports = BookedSeatController;