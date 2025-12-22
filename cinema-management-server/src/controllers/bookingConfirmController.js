// src/controllers/bookingConfirmController.js

const BookingConfirmService = require('../services/bookingConfirmService');

class BookingConfirmController {
  static async fetchMovieDetails(req, res) {
    const { movieId } = req.params;
    if (!movieId) {
      return res.status(400).json({ success: false, message: 'Thiếu movieId' });
    }

    try {
      const movie = await BookingConfirmService.getMovieDetails(movieId);
      if (!movie) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy phim' });
      }
      res.json({ success: true, data: movie });
    } catch (error) {
      console.error('Error in fetchMovieDetails:', error);
      res.status(500).json({ success: false, message: 'Lỗi server' });
    }
  }

  static async fetchCinemaDetails(req, res) {
    const { cinemaId } = req.params;
    if (!cinemaId) {
      return res.status(400).json({ success: false, message: 'Thiếu cinemaId' });
    }

    try {
      const cinema = await BookingConfirmService.getCinemaDetails(cinemaId);
      if (!cinema) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy rạp chiếu' });
      }
      res.json({ success: true, data: cinema });
    } catch (error) {
      console.error('Error in fetchCinemaDetails:', error);
      res.status(500).json({ success: false, message: 'Lỗi server' });
    }
  }

  static async fetchScreenDetails(req, res) {
    const { screenId } = req.params;
    if (!screenId) {
      return res.status(400).json({ success: false, message: 'Thiếu screenId' });
    }

    try {
      const screen = await BookingConfirmService.getScreenDetails(screenId);
      if (!screen) {
        return res.status(404).json({ success: false, message: 'Không tìm thấy màn hình chiếu' });
      }
      res.json({ success: true, data: screen });
    } catch (error) {
      console.error('Error in fetchScreenDetails:', error);
      res.status(500).json({ success: false, message: 'Lỗi server' });
    }
  }
}

module.exports = BookingConfirmController;