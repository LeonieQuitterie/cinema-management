const BookingService = require('../services/bookingService');

exports.createBooking = async (req, res) => {
  try {
    await BookingService.createBooking(req.body);
    res.status(201).json({ message: 'Booking created successfully' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Create booking failed' });
  }
};
