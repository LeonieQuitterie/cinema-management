const Redis = require('ioredis');
const redis = new Redis({
    host: 'localhost',
    port: 6379,
    retryStrategy: (times) => {
        if (times > 3) {
            console.error('Redis connection failed after 3 retries');
            return null;
        }
        return Math.min(times * 100, 2000);
    }
});

redis.on('error', (err) => {
    console.error('Redis Error:', err);
});

redis.on('connect', () => {
    console.log('✅ Redis connected successfully');
});

const SEAT_HOLD_TTL = 600; // 10 phút

function setupSeatSocket(io) {
    const seatNamespace = io.of('/seats');

    seatNamespace.on('connection', (socket) => {
        console.log('🔌 User connected:', socket.id);

        // 1. JOIN SHOWTIME ROOM
        socket.on('join-showtime', async (data) => {
            const { showtimeId } = data;
            socket.join(showtimeId);
            socket.showtimeId = showtimeId;
            console.log(`📍 Socket ${socket.id} joined showtime: ${showtimeId}`);

            // Gửi danh sách ghế đang held về cho client
            const heldSeats = await getHeldSeats(showtimeId);
            socket.emit('initial-held-seats', { seats: heldSeats });
        });

        // 2. HOLD SEAT
        socket.on('hold-seat', async (data) => {
            const { showtimeId, seatNumber } = data;
            const holdKey = `hold:${showtimeId}:${seatNumber}`;

            try {
                // Kiểm tra xem ghế có đang được hold bởi người khác không
                const currentHolder = await redis.get(holdKey);
                
                if (currentHolder && currentHolder !== socket.id) {
                    // Ghế đang được hold bởi người khác
                    socket.emit('hold-failed', { 
                        seatNumber, 
                        reason: 'Ghế đang được chọn bởi người khác' 
                    });
                    return;
                }

                // Hold ghế với TTL 10 phút
                await redis.setex(holdKey, SEAT_HOLD_TTL, socket.id);

                // Broadcast cho tất cả trong room (kể cả người gửi)
                seatNamespace.to(showtimeId).emit('seat-held', {
                    seatNumber,
                    holderId: socket.id
                });

                console.log(`✅ Seat ${seatNumber} held by ${socket.id}`);

            } catch (error) {
                console.error('Error holding seat:', error);
                socket.emit('hold-failed', { 
                    seatNumber, 
                    reason: 'Lỗi hệ thống' 
                });
            }
        });

        // 3. RELEASE SEAT
        socket.on('release-seat', async (data) => {
            const { showtimeId, seatNumber } = data;
            const holdKey = `hold:${showtimeId}:${seatNumber}`;

            try {
                const currentHolder = await redis.get(holdKey);

                // Chỉ release nếu chính user này đang hold
                if (currentHolder === socket.id) {
                    await redis.del(holdKey);
                    
                    seatNamespace.to(showtimeId).emit('seat-released', {
                        seatNumber
                    });

                    console.log(`🔓 Seat ${seatNumber} released by ${socket.id}`);
                }
            } catch (error) {
                console.error('Error releasing seat:', error);
            }
        });

        // 4. BOOK SEATS (sau khi thanh toán thành công)
        socket.on('book-seats', async (data) => {
            const { showtimeId, seatNumbers } = data;

            try {
                // Release các hold và broadcast seats booked
                for (const seatNumber of seatNumbers) {
                    const holdKey = `hold:${showtimeId}:${seatNumber}`;
                    await redis.del(holdKey);
                }

                seatNamespace.to(showtimeId).emit('seats-booked', {
                    seatNumbers
                });

                console.log(`🎟️ Seats booked: ${seatNumbers.join(', ')}`);

            } catch (error) {
                console.error('Error booking seats:', error);
            }
        });

        // 5. DISCONNECT - Release tất cả ghế của user
        socket.on('disconnect', async () => {
            console.log('🔌 User disconnected:', socket.id);
            
            if (socket.showtimeId) {
                await releaseAllSeatsForSocket(socket.showtimeId, socket.id, seatNamespace);
            }
        });
    });
}

// === HELPER FUNCTIONS ===

async function getHeldSeats(showtimeId) {
    try {
        const keys = await redis.keys(`hold:${showtimeId}:*`);
        const heldSeats = [];

        for (const key of keys) {
            const seatNumber = key.split(':')[2];
            const holderId = await redis.get(key);
            if (holderId) {
                heldSeats.push({ seatNumber, holderId });
            }
        }

        return heldSeats;
    } catch (error) {
        console.error('Error getting held seats:', error);
        return [];
    }
}

async function releaseAllSeatsForSocket(showtimeId, socketId, namespace) {
    try {
        const keys = await redis.keys(`hold:${showtimeId}:*`);
        const releasedSeats = [];

        for (const key of keys) {
            const holder = await redis.get(key);
            if (holder === socketId) {
                const seatNumber = key.split(':')[2];
                await redis.del(key);
                releasedSeats.push(seatNumber);
            }
        }

        if (releasedSeats.length > 0) {
            namespace.to(showtimeId).emit('seats-released-batch', {
                seatNumbers: releasedSeats
            });
            console.log(`🔓 Released ${releasedSeats.length} seats on disconnect`);
        }
    } catch (error) {
        console.error('Error releasing seats on disconnect:', error);
    }
}

module.exports = setupSeatSocket;