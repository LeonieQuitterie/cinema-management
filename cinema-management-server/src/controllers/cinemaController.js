// src/controllers/cinemaController.js
const CinemaService = require("../services/cinemaService");

class CinemaController {
  /**
   * GET /api/cinemas/movie/:movieId
   * Lấy danh sách rạp đang chiếu phim (từ hôm nay)
   */
  static async getCinemasByMovie(req, res) {
    try {
      const { movieId } = req.params;
      
      const cinemas = await CinemaService.getCinemasByMovieId(movieId);

      res.json({
        success: true,
        data: cinemas,
        count: cinemas.length
      });

    } catch (error) {
      console.error("Error in getCinemasByMovie:", error);
      res.status(500).json({
        success: false,
        message: error.message || "Lỗi khi lấy danh sách rạp",
        error: error.message
      });
    }
  }
}

module.exports = CinemaController;