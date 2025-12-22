// src/services/cinemaService.js
const db = require("../config/database");

class CinemaService {
  /**
   * Lấy danh sách rạp kèm phòng chiếu và sơ đồ ghế
   * @param {Object} filters - { city, cinemaId }
   * @returns {Array} Danh sách Cinema với cấu trúc giống mock
   */
  static async getCinemasWithScreens(filters = {}) {
    try {
      // 1. Query cinemas
      let cinemaQuery = `
        SELECT 
          id, 
          name, 
          address, 
          city, 
          logo_url as logoUrl
        FROM cinemas
        WHERE 1=1
      `;
      const cinemaParams = [];

      if (filters.city) {
        cinemaQuery += ` AND city = ?`;
        cinemaParams.push(filters.city);
      }

      if (filters.cinemaId) {
        cinemaQuery += ` AND id = ?`;
        cinemaParams.push(filters.cinemaId);
      }

      cinemaQuery += ` ORDER BY name`;

      const [cinemas] = await db.query(cinemaQuery, cinemaParams);

      // 2. Lấy screens cho tất cả cinemas
      if (cinemas.length === 0) {
        return [];
      }

      const cinemaIds = cinemas.map(c => c.id);
      const [screens] = await db.query(
        `SELECT 
          id,
          name,
          cinema_id as cinemaId,
          row_count as rowCount,
          column_count as columnCount,
          total_seats as totalSeats
        FROM screens
        WHERE cinema_id IN (?)
        ORDER BY cinema_id, name`,
        [cinemaIds]
      );

      // 3. Lấy tất cả seats cho các screens
      if (screens.length > 0) {
        const screenIds = screens.map(s => s.id);
        const [seats] = await db.query(
          `SELECT 
            id,
            screen_id as screenId,
            seat_number as seatNumber,
            seat_type as seatType,
            price,
            row_index as rowIndex,
            col_index as colIndex
          FROM seats
          WHERE screen_id IN (?)
          ORDER BY screen_id, row_index, col_index`,
          [screenIds]
        );

        // Group seats by screenId
        const seatsByScreen = {};
        seats.forEach(seat => {
          if (!seatsByScreen[seat.screenId]) {
            seatsByScreen[seat.screenId] = [];
          }
          seatsByScreen[seat.screenId].push(seat);
        });

        // 4. Build SeatLayout cho mỗi screen
        screens.forEach(screen => {
          const screenSeats = seatsByScreen[screen.id] || [];
          screen.seatLayout = this.buildSeatLayout(
            screen.rowCount,
            screen.columnCount,
            screenSeats
          );
        });
      }

      // 5. Group screens by cinemaId
      const screensByCinema = {};
      screens.forEach(screen => {
        if (!screensByCinema[screen.cinemaId]) {
          screensByCinema[screen.cinemaId] = [];
        }
        screensByCinema[screen.cinemaId].push(screen);
      });

      // 6. Gắn screens vào cinemas
      cinemas.forEach(cinema => {
        cinema.screens = screensByCinema[cinema.id] || [];
      });

      return cinemas;

    } catch (error) {
      console.error("Error in getCinemasWithScreens:", error);
      throw error;
    }
  }

  /**
   * Build ma trận ghế từ danh sách seats
   * @param {number} rows - Số hàng
   * @param {number} columns - Số cột  
   * @param {Array} seats - Danh sách ghế
   * @returns {Object} SeatLayout { rows, columns, seats: [[Seat]] }
   */
  static buildSeatLayout(rowCount, columnCount, seats) {
    const seatMatrix = [];
    for (let i = 0; i < rowCount; i++) {
      seatMatrix.push(new Array(columnCount).fill(null));
    }

    seats.forEach(seat => {
      const { rowIndex, colIndex } = seat;
      if (
        rowIndex >= 0 && rowIndex < rowCount &&
        colIndex >= 0 && colIndex < columnCount
      ) {
        seatMatrix[rowIndex][colIndex] = {
          seatNumber: seat.seatNumber,
          seatType: seat.seatType,
          price: parseFloat(seat.price),
          rowIndex,
          colIndex
        };
      }
    });

    return {
      rowCount,
      columnCount,
      seats: seatMatrix
    };
  }

  /**
   * Lấy thông tin 1 rạp cụ thể
   */
  static async getCinemaById(cinemaId) {
    const cinemas = await this.getCinemasWithScreens({ cinemaId });
    return cinemas.length > 0 ? cinemas[0] : null;
  }

  /**
   * Lấy danh sách rạp theo thành phố (không kèm seat layout - nhẹ hơn)
   */
  static async getCinemasByCity(city) {
    try {
      const [cinemas] = await db.query(
        `SELECT 
          c.id,
          c.name,
          c.address,
          c.city,
          c.logo_url as logoUrl,
          COUNT(s.id) as screenCount
        FROM cinemas c
        LEFT JOIN screens s ON c.id = s.cinema_id
        WHERE c.city = ?
        GROUP BY c.id, c.name, c.address, c.city, c.logo_url
        ORDER BY c.name`,
        [city]
      );
      return cinemas;
    } catch (error) {
      console.error("Error in getCinemasByCity:", error);
      throw error;
    }
  }
}

module.exports = CinemaService;