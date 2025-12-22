const db = require('../../config/database');

class ShowtimeService {
    /**
     * Get all showtimes with movie, cinema, and screen details
     */
    async getAllShowtimes(filters = {}) {
        const { date, cinemaId, screenId, movieId } = filters;
        
        let query = `
            SELECT 
                st.id,
                st.movie_id,
                st.screen_id,
                st.start_time,
                st.end_time,
                st.base_price,
                st.format,
                m.title as movie_title,
                m.duration as movie_duration,
                m.poster_url,
                s.name as screen_name,
                s.total_seats,
                c.id as cinema_id,
                c.name as cinema_name,
                (
                    SELECT COUNT(*) 
                    FROM booking_seats bs
                    JOIN bookings b ON bs.booking_id = b.id
                    WHERE b.showtime_id = st.id 
                    AND b.payment_status IN ('PAID', 'PENDING')
                ) as booked_seats_count
            FROM showtimes st
            JOIN movies m ON st.movie_id = m.id
            JOIN screens s ON st.screen_id = s.id
            JOIN cinemas c ON s.cinema_id = c.id
            WHERE 1=1
        `;
        
        const params = [];
        
        if (date) {
            query += ' AND DATE(st.start_time) = ?';
            params.push(date);
        }
        
        if (cinemaId) {
            query += ' AND c.id = ?';
            params.push(cinemaId);
        }
        
        if (screenId) {
            query += ' AND st.screen_id = ?';
            params.push(screenId);
        }
        
        if (movieId) {
            query += ' AND st.movie_id = ?';
            params.push(movieId);
        }
        
        query += ' ORDER BY st.start_time ASC';
        
        const [showtimes] = await db.query(query, params);
        
        return showtimes.map(st => ({
            ...st,
            available_seats: st.total_seats - (st.booked_seats_count || 0)
        }));
    }

    /**
     * Get showtime by ID
     */
    async getShowtimeById(showtimeId) {
        const query = `
            SELECT 
                st.*,
                m.title as movie_title,
                m.duration as movie_duration,
                s.name as screen_name,
                s.total_seats,
                c.id as cinema_id,
                c.name as cinema_name
            FROM showtimes st
            JOIN movies m ON st.movie_id = m.id
            JOIN screens s ON st.screen_id = s.id
            JOIN cinemas c ON s.cinema_id = c.id
            WHERE st.id = ?
        `;
        
        const [showtimes] = await db.query(query, [showtimeId]);
        return showtimes.length > 0 ? showtimes[0] : null;
    }

    /**
     * Create showtime
     */
    async createShowtime(showtimeData) {
        const { v4: uuidv4 } = require('uuid');
        const showtimeId = uuidv4();
        
        const query = `
            INSERT INTO showtimes (
                id, movie_id, screen_id, start_time, end_time, base_price, format
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        `;
        
        await db.query(query, [
            showtimeId,
            showtimeData.movie_id,
            showtimeData.screen_id,
            showtimeData.start_time,
            showtimeData.end_time,
            showtimeData.base_price,
            showtimeData.format
        ]);
        
        return await this.getShowtimeById(showtimeId);
    }

    /**
     * Create multiple showtimes (bulk)
     */
    async createBulkShowtimes(showtimesArray) {
        const connection = await db.getConnection();
        
        try {
            await connection.beginTransaction();
            
            const createdShowtimes = [];
            const { v4: uuidv4 } = require('uuid');
            
            for (const showtimeData of showtimesArray) {
                const showtimeId = uuidv4();
                
                const query = `
                    INSERT INTO showtimes (
                        id, movie_id, screen_id, start_time, end_time, base_price, format
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                `;
                
                await connection.query(query, [
                    showtimeId,
                    showtimeData.movie_id,
                    showtimeData.screen_id,
                    showtimeData.start_time,
                    showtimeData.end_time,
                    showtimeData.base_price,
                    showtimeData.format
                ]);
                
                createdShowtimes.push(showtimeId);
            }
            
            await connection.commit();
            return createdShowtimes;
            
        } catch (error) {
            await connection.rollback();
            throw error;
        } finally {
            connection.release();
        }
    }

    /**
     * Update showtime
     */
    async updateShowtime(showtimeId, showtimeData) {
        const updates = [];
        const values = [];
        
        if (showtimeData.movie_id !== undefined) {
            updates.push('movie_id = ?');
            values.push(showtimeData.movie_id);
        }
        if (showtimeData.screen_id !== undefined) {
            updates.push('screen_id = ?');
            values.push(showtimeData.screen_id);
        }
        if (showtimeData.start_time !== undefined) {
            updates.push('start_time = ?');
            values.push(showtimeData.start_time);
        }
        if (showtimeData.end_time !== undefined) {
            updates.push('end_time = ?');
            values.push(showtimeData.end_time);
        }
        if (showtimeData.base_price !== undefined) {
            updates.push('base_price = ?');
            values.push(showtimeData.base_price);
        }
        if (showtimeData.format !== undefined) {
            updates.push('format = ?');
            values.push(showtimeData.format);
        }
        
        if (updates.length === 0) {
            return await this.getShowtimeById(showtimeId);
        }
        
        values.push(showtimeId);
        const query = `UPDATE showtimes SET ${updates.join(', ')} WHERE id = ?`;
        
        const [result] = await db.query(query, values);
        
        if (result.affectedRows === 0) {
            return null;
        }
        
        return await this.getShowtimeById(showtimeId);
    }

    /**
     * Delete showtime
     */
    async deleteShowtime(showtimeId) {
        const query = 'DELETE FROM showtimes WHERE id = ?';
        const [result] = await db.query(query, [showtimeId]);
        return result.affectedRows > 0;
    }

    /**
     * Get showtime statistics
     */
    async getShowtimeStats(date) {
        let query = `
            SELECT 
                COUNT(*) as total,
                SUM(CASE 
                    WHEN NOW() > end_time THEN 1 
                    ELSE 0 
                END) as completed,
                SUM(CASE 
                    WHEN NOW() BETWEEN start_time AND end_time THEN 1 
                    ELSE 0 
                END) as ongoing,
                SUM(CASE 
                    WHEN NOW() < start_time THEN 1 
                    ELSE 0 
                END) as upcoming
            FROM showtimes
        `;
        
        const params = [];
        
        if (date) {
            query += ' WHERE DATE(start_time) = ?';
            params.push(date);
        }
        
        const [results] = await db.query(query, params);
        return results[0];
    }
}

module.exports = new ShowtimeService();