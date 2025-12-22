// D:\cinema-management\cinema-management-server\src\socket\paymentSocket.js

const BookingService = require('../services/bookingService');

function setupPaymentSocket(io) {
  const paymentNamespace = io.of('/payment');

  paymentNamespace.on('connection', (socket) => {
    console.log('🔌 Payment client connected:', socket.id);

    // Client join room của booking để nhận updates
    socket.on('join-booking', async (bookingId) => {
      socket.join(`booking:${bookingId}`);
      socket.bookingId = bookingId;
      console.log(`📌 Socket ${socket.id} joined booking: ${bookingId}`);

      // Gửi trạng thái hiện tại về cho client (nếu đã thanh toán rồi)
      try {
        const booking = await BookingService.getBookingById(bookingId);
        if (booking && booking.payment_status === 'PAID') {
          socket.emit('payment:status', {
            bookingId: booking.id,
            status: 'SUCCESS',
            amount: booking.total_price,
            transactionId: booking.payment_transaction_id,
            timestamp: booking.payment_time
          });
          console.log(`✅ Sent existing payment status to ${socket.id}`);
        }
      } catch (error) {
        console.error('Error fengrok http 3000tching booking status:', error);
      }
    });

    socket.on('disconnect', () => {
      console.log('❌ Payment client disconnected:', socket.id);
    });
  });

  return paymentNamespace;
}

module.exports = setupPaymentSocket;