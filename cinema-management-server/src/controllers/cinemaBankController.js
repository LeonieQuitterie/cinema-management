// src/controllers/cinemaBankController.js

const CinemaBankService = require('../services/cinemaBankService');

class CinemaBankController {
  static async getBankInfo(req, res) {
    const { cinemaId } = req.params;

    if (!cinemaId) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu cinemaId'
      });
    }

    try {
      const bankInfo = await CinemaBankService.getBankInfo(cinemaId);

      if (!bankInfo) {
        return res.status(404).json({
          success: false,
          message: 'Không tìm thấy thông tin ngân hàng cho rạp này'
        });
      }

      res.json({
        success: true,
        data: bankInfo
      });

    } catch (error) {
      console.error('Error in getBankInfo:', error);
      res.status(500).json({
        success: false,
        message: 'Lỗi server khi lấy thông tin ngân hàng'
      });
    }
  }
}

module.exports = CinemaBankController;