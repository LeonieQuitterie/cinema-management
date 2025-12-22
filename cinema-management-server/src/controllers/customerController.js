// src/controllers/customerController.js

const CustomerService = require('../services/customerService');

class CustomerController {
  static async getMyProfile(req, res) {
    const userId = req.user?.id;

    if (!userId) {
      return res.status(401).json({
        success: false,
        message: 'Chưa xác thực người dùng'
      });
    }

    try {
      const customer = await CustomerService.getCustomerProfile(userId);

      if (!customer) {
        return res.status(404).json({
          success: false,
          message: 'Không tìm thấy thông tin khách hàng'
        });
      }

      res.json({
        success: true,
        data: customer
      });
    } catch (error) {
      console.error('Error in CustomerController.getMyProfile:', error);
      res.status(500).json({
        success: false,
        message: 'Lỗi server khi lấy thông tin khách hàng'
      });
    }
  }
}

module.exports = CustomerController;