// D:\cinema-management\cinema-management-server\src\routes\paymentRoutes.js

const express = require('express');
const router = express.Router();
const crypto = require('crypto');
const BookingService = require('../services/bookingService');

// Socket.IO instance sẽ được inject từ server.js
let paymentNamespace;
const setPaymentNamespace = (namespace) => { paymentNamespace = namespace; };

// ✅ WEBHOOK từ PayOS
router.post('/payos-webhook', async (req, res) => {
  try {
    console.log('📨 Received PayOS webhook');
    console.log('Headers:', req.headers);
    console.log('Body:', JSON.stringify(req.body, null, 2));

    const { code, desc, success, data, signature } = req.body;

    // 1. Verify signature
    if (!verifyPayOSSignature(data, signature)) {
      console.error('❌ Invalid PayOS signature');
      return res.status(401).json({ error: 'Invalid signature' });
    }

    // 2. Check success status
    if (!success || code !== '00') {
      console.warn(`⚠️ Payment failed: ${desc}`);
      return res.json({ success: true }); // Vẫn trả 200 để PayOS không gửi lại
    }

    // 3. Extract payment data
    const {
      orderCode,
      amount,
      description,
      reference,
      transactionDateTime,
      code: paymentCode,
      desc: paymentDesc
    } = data;

    console.log(`💰 Processing payment for orderCode: ${orderCode}`);
    console.log(`   Amount: ${amount.toLocaleString('vi-VN')} VND`);
    console.log(`   Reference: ${reference}`);
    console.log(`   Description: ${description}`);

    // 4. Extract booking ID từ description
    // Description format: "CINEMA BOOK123456789" hoặc "BOOK123456789"
    const bookingId = extractBookingId(description);
    
    if (!bookingId) {
      console.warn('⚠️ No booking ID found in description:', description);
      // Thử tìm theo orderCode (nếu Q lưu orderCode khi tạo booking)
      const bookingByOrder = await BookingService.getBookingByOrderCode(orderCode);
      if (!bookingByOrder) {
        return res.json({ success: true });
      }
      bookingId = bookingByOrder.id;
    }

    console.log(`📌 Found booking ID: ${bookingId}`);

    // 5. Get booking from database
    const booking = await BookingService.getBookingById(bookingId);

    if (!booking) {
      console.warn(`❌ Booking not found: ${bookingId}`);
      return res.json({ success: true });
    }

    // 6. Check if already paid
    if (booking.payment_status === 'PAID') {
      console.log(`✓ Booking ${bookingId} already paid`);
      return res.json({ success: true });
    }

    // 7. Verify amount
    const expectedAmount = Math.round(booking.total_price);
    if (Math.abs(amount - expectedAmount) > 1000) {
      console.warn(`⚠️ Amount mismatch for ${bookingId}:`);
      console.warn(`   Expected: ${expectedAmount.toLocaleString('vi-VN')}`);
      console.warn(`   Received: ${amount.toLocaleString('vi-VN')}`);
      // TODO: Lưu vào pending_payments table để admin review
      return res.json({ success: true });
    }

    // 8. Update booking payment status
    await BookingService.updatePaymentStatus(bookingId, {
      status: 'PAID',
      paymentTime: new Date(transactionDateTime),
      transactionId: reference,
      orderCode: orderCode
    });

    console.log(`✅ Payment confirmed for booking ${bookingId}`);

    // 9. Emit Socket.IO event to client
    if (paymentNamespace) {
      paymentNamespace.to(`booking:${bookingId}`).emit('payment:status', {
        bookingId: bookingId,
        status: 'SUCCESS',
        amount: amount,
        transactionId: reference,
        orderCode: orderCode,
        timestamp: new Date().toISOString()
      });
      console.log(`📡 Emitted payment:status event for ${bookingId}`);
    } else {
      console.warn('⚠️ Payment namespace not initialized');
    }

    // 10. TODO: Gửi email/SMS confirmation
    // await sendPaymentConfirmation(booking);

    res.json({ success: true });

  } catch (error) {
    console.error('🔴 Webhook processing error:', error);
    res.status(500).json({ error: error.message });
  }
});

// ✅ API polling backup (nếu Socket.IO fail)
router.get('/status/:bookingId', async (req, res) => {
  try {
    const booking = await BookingService.getBookingById(req.params.bookingId);
    
    if (!booking) {
      return res.status(404).json({ error: 'Booking not found' });
    }

    res.json({
      bookingId: booking.id,
      paymentStatus: booking.payment_status,
      paidAmount: booking.payment_status === 'PAID' ? booking.total_price : null,
      paidAt: booking.payment_time,
      transactionId: booking.payment_transaction_id
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// === HELPER FUNCTIONS ===

function extractBookingId(description) {
  // Description formats:
  // "CINEMA BOOK123456789"
  // "BOOK123456789"
  // "Thanh toan ve BOOK123456789"
  
  const match = description.match(/BOOK[_\d]{9,15}/i);
  return match ? match[0].toUpperCase() : null;
}

function verifyPayOSSignature(data, receivedSignature) {
  // PayOS signature verification
  // Format: sortedData + checksumKey → sha256
  
  const checksumKey = process.env.PAYOS_CHECKSUM_KEY;
  
  if (!checksumKey) {
    console.error('❌ PAYOS_CHECKSUM_KEY not configured');
    return false;
  }

  try {
    // Sort object keys và build string
    const sortedKeys = Object.keys(data).sort();
    const signString = sortedKeys
      .map(key => `${key}=${data[key]}`)
      .join('&');
    
    // PayOS uses: sha256(signString + checksumKey)
    const computedSignature = crypto
      .createHash('sha256')
      .update(signString + checksumKey)
      .digest('hex');
    
    const isValid = computedSignature === receivedSignature;
    
    if (!isValid) {
      console.warn('Signature mismatch:');
      console.warn('  Computed:', computedSignature);
      console.warn('  Received:', receivedSignature);
    }
    
    return isValid;
  } catch (error) {
    console.error('Error verifying signature:', error);
    return false;
  }
}

module.exports = { router, setPaymentNamespace };