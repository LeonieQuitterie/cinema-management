// src/controllers/cinemaController.js
const CinemaService = require("../services/cinemaService");

class CinemaController {
  /**
   * GET /api/cinemas
   * Lấy danh sách rạp kèm screens và seat layout
   */
  static async getCinemas(req, res) {
    try {
      const { city, cinemaId, includeSeats } = req.query;

      // Nếu không cần seat layout, dùng query nhẹ hơn
      if (includeSeats === 'false' && city) {
        const cinemas = await CinemaService.getCinemasByCity(city);
        return res.json({
          success: true,
          data: cinemas,
          count: cinemas.length
        });
      }

      // Lấy full data với seat layout
      const filters = {};
      if (city) filters.city = city;
      if (cinemaId) filters.cinemaId = cinemaId;

      const cinemas = await CinemaService.getCinemasWithScreens(filters);

      res.json({
        success: true,
        data: cinemas,
        count: cinemas.length
      });

    } catch (error) {
      console.error("Error in getCinemas:", error);
      res.status(500).json({
        success: false,
        message: "Lỗi khi lấy danh sách rạp",
        error: error.message
      });
    }
  }

  /**
   * GET /api/cinemas/:id
   * Lấy thông tin chi tiết 1 rạp
   */
  static async getCinemaById(req, res) {
    try {
      const { id } = req.params;
      const cinema = await CinemaService.getCinemaById(id);

      if (!cinema) {
        return res.status(404).json({
          success: false,
          message: "Không tìm thấy rạp chiếu"
        });
      }

      res.json({
        success: true,
        data: cinema
      });

    } catch (error) {
      console.error("Error in getCinemaById:", error);
      res.status(500).json({
        success: false,
        message: "Lỗi khi lấy thông tin rạp",
        error: error.message
      });
    }
  }

  /**
   * GET /api/cinemas/city/:city
   * Lấy danh sách rạp theo thành phố (nhẹ)
   */
  static async getCinemasByCity(req, res) {
    try {
      const { city } = req.params;
      const cinemas = await CinemaService.getCinemasByCity(city);

      res.json({
        success: true,
        data: cinemas,
        count: cinemas.length
      });

    } catch (error) {
      console.error("Error in getCinemasByCity:", error);
      res.status(500).json({
        success: false,
        message: "Lỗi khi lấy danh sách rạp theo thành phố",
        error: error.message
      });
    }
  }
}

module.exports = CinemaController;