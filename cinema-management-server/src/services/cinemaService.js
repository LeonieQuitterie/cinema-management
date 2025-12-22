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
          cinema_id,
          row_count,
          column_count,
          total_seats
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
            screen_id,
            seat_number,
            seat_type,
            price,
            row_index,
            col_index
          FROM seats
          WHERE screen_id IN (?)
          ORDER BY screen_id, row_index, col_index`,
          [screenIds]
        );

        // Group seats by screenId
        const seatsByScreen = {};
        seats.forEach(seat => {
          if (!seatsByScreen[seat.screen_id]) {
            seatsByScreen[seat.screen_id] = [];
          }
          seatsByScreen[seat.screen_id].push(seat);
        });

        // 4. Build SeatLayout cho mỗi screen
        screens.forEach(screen => {
          const screenSeats = seatsByScreen[screen.id] || [];
          screen.seatLayout = this.buildSeatLayout(
            screen.row_count,
            screen.column_count,
            screenSeats
          );
        });
      }

      // 5. Group screens by cinemaId
      const screensByCinema = {};
      screens.forEach(screen => {
        if (!screensByCinema[screen.cinema_id]) {
          screensByCinema[screen.cinema_id] = [];
        }
        screensByCinema[screen.cinema_id].push(screen);
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
   * Lấy danh sách rạp đang chiếu phim (từ hôm nay trở đi)
   * @param {string} movieId - ID của phim
   * @returns {Array} Danh sách Cinema kèm showtimes
   */
  static async getCinemasByMovieId(movieId, admin = false) {
    try {
      // 1. Lấy tên phim
      const [movies] = await db.query(
        `SELECT title FROM movies WHERE id = ?`,
        [movieId]
      );

      if (movies.length === 0) {
        throw new Error("Không tìm thấy phim");
      }

      const movieTitle = movies[0].title;
      const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD

      // 2. Query tất cả rạp đang chiếu phim này (từ hôm nay)
      const [cinemas] = await db.query(
        `SELECT DISTINCT
          c.id,
          c.name,
          c.address,
          c.city,
          c.logo_url as logoUrl
        FROM cinemas c
        INNER JOIN screens sc ON c.id = sc.cinema_id
        INNER JOIN showtimes st ON sc.id = st.screen_id
        INNER JOIN movies m ON st.movie_id = m.id
        WHERE m.title = ?
          AND DATE(st.start_time) >= ?
        ORDER BY c.city, c.name`,
        [movieTitle, today]
      );

      if (cinemas.length === 0) {
        return [];
      }

      const cinemaIds = cinemas.map(c => c.id);

      ///////////
      // 3. Lấy screens của các rạp này
      let screens = [];
      if (admin == true) {
        const [rows] = await db.query(
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
        screens = rows;
      } else {
        const [rows] = await db.query(
            `SELECT DISTINCT
            sc.id,
            sc.name,
            sc.cinema_id as cinemaId,
            sc.row_count as rowCount,
            sc.column_count as columnCount,
            sc.total_seats as totalSeats
          FROM screens sc
          INNER JOIN showtimes st ON sc.id = st.screen_id
          INNER JOIN movies m ON st.movie_id = m.id
          WHERE sc.cinema_id IN (?)
            AND m.title = ?
            AND DATE(st.start_time) >= ?
          ORDER BY sc.cinema_id, sc.name`,
          [cinemaIds, movieTitle, today]
        );
        screens = rows;
      }

      // 4. THÊM MỚI: Lấy showtimes
      if (screens.length > 0) {
        const screenIds = screens.map(s => s.id);
        
        const [showtimes] = await db.query(
          `SELECT 
            st.id,
            st.screen_id as screenId,
            st.start_time as startTime,
            st.end_time as endTime,
            st.base_price as basePrice,
            st.format
          FROM showtimes st
          INNER JOIN movies m ON st.movie_id = m.id
          WHERE st.screen_id IN (?)
            AND m.title = ?
            AND DATE(st.start_time) >= ?
          ORDER BY st.start_time`,
          [screenIds, movieTitle, today]
        );

        // Group showtimes theo screenId
        const showtimesByScreen = {};
        showtimes.forEach(st => {
          if (!showtimesByScreen[st.screenId]) {
            showtimesByScreen[st.screenId] = [];
          }
          showtimesByScreen[st.screenId].push({
            id: st.id,
            startTime: st.startTime,
            endTime: st.endTime,
            basePrice: parseFloat(st.basePrice),
            format: st.format
          });
        });

        // 5. Lấy seats
        const [seats] = await db.query(
          `SELECT 
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

        // Group seats theo screenId
        const seatsByScreen = {};
        seats.forEach(seat => {
          if (!seatsByScreen[seat.screenId]) {
            seatsByScreen[seat.screenId] = [];
          }
          seatsByScreen[seat.screenId].push(seat);
        });

        // 6. Build SeatLayout và gắn showtimes
        screens.forEach(screen => {
          const screenSeats = seatsByScreen[screen.id] || [];
          screen.seatLayout = this.buildSeatLayout(
            screen.rowCount,
            screen.columnCount,
            screenSeats
          );
          
          // Gắn showtimes vào screen
          screen.showtimes = showtimesByScreen[screen.id] || [];
          
          // Đổi tên để match với Java model
          screen.rows = screen.rowCount;
          screen.columns = screen.columnCount;
          delete screen.rowCount;
          delete screen.columnCount;
        });
      }

      // 7. Group screens theo cinemaId
      const screensByCinema = {};
      screens.forEach(screen => {
        if (!screensByCinema[screen.cinemaId]) {
          screensByCinema[screen.cinemaId] = [];
        }
        screensByCinema[screen.cinemaId].push(screen);
      });

      // 8. Gắn screens vào cinemas
      cinemas.forEach(cinema => {
        cinema.screens = screensByCinema[cinema.id] || [];
      });

      return cinemas;

    } catch (error) {
      console.error("Error in getCinemasByMovieId:", error);
      throw error;
    }
  }

  /**
   * Build ma trận ghế
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
}

module.exports = CinemaService;