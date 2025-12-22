const db = require('../../config/database');

class MovieService {
    /**
     * Get all movies with genres
     */
    async getAllMovies() {
        const query = `
            SELECT 
                m.id,
                m.title,
                m.description,
                m.duration,
                m.poster_url,
                m.release_date,
                m.language,
                m.age_rating,
                m.age_rating_description,
                m.average_rating,
                m.total_ratings,
                m.trailer_url,
                m.created_at,
                CASE 
                    WHEN m.release_date IS NULL THEN 'COMING_SOON'
                    WHEN m.release_date <= CURDATE() THEN 'NOW_SHOWING'
                    ELSE 'COMING_SOON'
                END as status,
                JSON_ARRAYAGG(
                    IF(g.id IS NOT NULL,
                        JSON_OBJECT('id', g.id, 'name', g.name),
                        NULL
                    )
                ) as genres
            FROM movies m
            LEFT JOIN movie_genres mg ON m.id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.id
            GROUP BY m.id
            ORDER BY m.release_date DESC, m.created_at DESC
        `;

        const [movies] = await db.query(query);

        // Clean up genres array (remove null values for movies without genres)
        return movies.map(movie => ({
            ...movie,
            genres: movie.genres ? movie.genres.filter(g => g !== null) : []
        }));
    }

    /**
     * Get movie by ID with genres
     */
    async getMovieById(movieId) {
        const query = `
            SELECT 
                m.id,
                m.title,
                m.description,
                m.duration,
                m.poster_url,
                m.release_date,
                m.language,
                m.age_rating,
                m.age_rating_description,
                m.average_rating,
                m.total_ratings,
                m.trailer_url,
                m.created_at,
                CASE 
                    WHEN m.release_date IS NULL THEN 'COMING_SOON'
                    WHEN m.release_date <= CURDATE() THEN 'NOW_SHOWING'
                    ELSE 'COMING_SOON'
                END as status,
                JSON_ARRAYAGG(
                    IF(g.id IS NOT NULL,
                        JSON_OBJECT('id', g.id, 'name', g.name),
                        NULL
                    )
                ) as genres
            FROM movies m
            LEFT JOIN movie_genres mg ON m.id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.id
            WHERE m.id = ?
            GROUP BY m.id
        `;

        const [movies] = await db.query(query, [movieId]);

        if (movies.length === 0) {
            return null;
        }

        return {
            ...movies[0],
            genres: movies[0].genres ? movies[0].genres.filter(g => g !== null) : []
        };
    }

    /**
     * Create new movie
     */
    async createMovie(movieData) {
        const connection = await db.getConnection();
        console.log(movieData)
        try {
            await connection.beginTransaction();

            const { v4: uuidv4 } = require('uuid');
            const movieId = uuidv4();

            // Insert movie
            const insertMovieQuery = `
                INSERT INTO movies (
                    id, title, description, duration, poster_url, 
                    release_date, language, age_rating, age_rating_description, 
                    trailer_url
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            `;

            await connection.query(insertMovieQuery, [
                movieId,
                movieData.title,
                movieData.description || null,
                movieData.duration,
                movieData.poster_url || null,
                movieData.release_date || null,
                movieData.language || null,
                movieData.age_rating || null,
                movieData.age_rating_description || null,
                movieData.trailer_url || null
            ]);

            // Insert genres if provided
            const genres = movieData.genres;
            if (Array.isArray(genres) && genres.length > 0) {
                const insertGenreQuery = 'INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)';
                
                for (const genre of genres) {
                    await connection.query(insertGenreQuery, [
                        movieId,
                        genre.id
                    ]);
                }
            }

            await connection.commit();

            // Fetch and return the created movie
            return await this.getMovieById(movieId);

        } catch (error) {
            await connection.rollback();
            throw error;
        } finally {
            connection.release();
        }
    }

    /**
     * Update movie
     */
    async updateMovie(movieId, movieData) {
        const connection = await db.getConnection();

        try {
            await connection.beginTransaction();

            // Build dynamic UPDATE query
            const updates = [];
            const values = [];

            if (movieData.title !== undefined) {
                updates.push('title = ?');
                values.push(movieData.title);
            }
            if (movieData.description !== undefined) {
                updates.push('description = ?');
                values.push(movieData.description);
            }
            if (movieData.duration !== undefined) {
                updates.push('duration = ?');
                values.push(movieData.duration);
            }
            if (movieData.poster_url !== undefined) {
                updates.push('poster_url = ?');
                values.push(movieData.poster_url);
            }
            if (movieData.release_date !== undefined) {
                updates.push('release_date = ?');
                values.push(movieData.release_date);
            }
            if (movieData.language !== undefined) {
                updates.push('language = ?');
                values.push(movieData.language);
            }
            if (movieData.age_rating !== undefined) {
                updates.push('age_rating = ?');
                values.push(movieData.age_rating);
            }
            if (movieData.age_rating_description !== undefined) {
                updates.push('age_rating_description = ?');
                values.push(movieData.age_rating_description);
            }
            if (movieData.trailer_url !== undefined) {
                updates.push('trailer_url = ?');
                values.push(movieData.trailer_url);
            }

            if (updates.length > 0) {
                values.push(movieId);
                const updateQuery = `UPDATE movies SET ${updates.join(', ')} WHERE id = ?`;
                const [result] = await connection.query(updateQuery, values);

                if (result.affectedRows === 0) {
                    await connection.rollback();
                    return null; // Movie not found
                }
            }

            const genres = movieData.genres
            // Update genres if provided
            if (Array.isArray(genres)) {
                // Delete existing genres
                await connection.query('DELETE FROM movie_genres WHERE movie_id = ?', [movieId]);

                // Insert new genres
                if (genres.length > 0) {
                    const insertGenreQuery = 'INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)';
                    
                    // for (const genreId of movieData.genre_ids) {
                    //     await connection.query(insertGenreQuery, [movieId, genreId]);
                    // }
                    for (const genre of genres) {
                        await connection.query(insertGenreQuery, [
                            movieId, genre.id
                        ]);
                    }
                }
            }

            await connection.commit();

            // Fetch and return updated movie
            return await this.getMovieById(movieId);

        } catch (error) {
            await connection.rollback();
            throw error;
        } finally {
            connection.release();
        }
    }

    /**
     * Delete movie
     */
    async deleteMovie(movieId) {
        const query = 'DELETE FROM movies WHERE id = ?';
        const [result] = await db.query(query, [movieId]);

        return result.affectedRows > 0;
    }

    /**
     * Search movies with filters and pagination
     */
    async searchMovies(filters = {}) {
        const { 
            q, 
            status, 
            ageRating, 
            genreName, 
            page = 1, 
            limit = 10, 
            sortBy = 'release_date', 
            sortOrder = 'DESC' 
        } = filters;

        const offset = (page - 1) * limit;
        const whereClauses = [];
        const values = [];

        // Build WHERE clauses
        if (q) {
            whereClauses.push('LOWER(m.title) LIKE LOWER(?)');
            values.push(`%${q}%`);
        }

        if (ageRating) {
            whereClauses.push('m.age_rating = ?');
            values.push(ageRating);
        }

        // Build base query
        let query = `
            SELECT 
                m.id,
                m.title,
                m.description,
                m.duration,
                m.poster_url,
                m.release_date,
                m.language,
                m.age_rating,
                m.age_rating_description,
                m.average_rating,
                m.total_ratings,
                m.trailer_url,
                m.created_at,
                CASE 
                    WHEN m.release_date IS NULL THEN 'COMING_SOON'
                    WHEN m.release_date <= CURDATE() THEN 'NOW_SHOWING'
                    ELSE 'COMING_SOON'
                END as status,
                JSON_ARRAYAGG(
                    IF(g.id IS NOT NULL,
                        JSON_OBJECT('id', g.id, 'name', g.name),
                        NULL
                    )
                ) as genres
            FROM movies m
            LEFT JOIN movie_genres mg ON m.id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.id
        `;

        if (whereClauses.length > 0) {
            query += ' WHERE ' + whereClauses.join(' AND ');
        }

        query += ' GROUP BY m.id';

        // Add HAVING clause for status and genre filtering
        const havingClauses = [];

        if (status) {
            havingClauses.push('status = ?');
            values.push(status);
        }

        if (genreName) {
            havingClauses.push('JSON_SEARCH(genres, "one", ?) IS NOT NULL');
            values.push(genreName);
        }

        if (havingClauses.length > 0) {
            query += ' HAVING ' + havingClauses.join(' AND ');
        }

        // Add sorting
        const allowedSortColumns = ['title', 'release_date', 'average_rating', 'created_at'];
        const sortColumn = allowedSortColumns.includes(sortBy) ? sortBy : 'release_date';
        const sortDirection = sortOrder.toUpperCase() === 'ASC' ? 'ASC' : 'DESC';

        query += ` ORDER BY m.${sortColumn} ${sortDirection}`;

        // Get total count (without pagination)
        const countQuery = `SELECT COUNT(*) as total FROM (${query}) as subquery`;
        const [countResult] = await db.query(countQuery, values);
        const total = countResult[0].total;

        // Add pagination
        query += ' LIMIT ? OFFSET ?';
        values.push(limit, offset);

        const [movies] = await db.query(query, values);

        // Clean up genres
        const formattedMovies = movies.map(movie => ({
            ...movie,
            genres: movie.genres ? movie.genres.filter(g => g !== null) : []
        }));

        return {
            total,
            page: parseInt(page),
            limit: parseInt(limit),
            totalPages: Math.ceil(total / limit),
            data: formattedMovies
        };
    }

    /**
     * Get movie statistics
     */
    async getMovieStats() {
        const query = `
            SELECT 
                COUNT(*) as total,
                SUM(CASE 
                    WHEN release_date IS NOT NULL AND release_date <= CURDATE() 
                    THEN 1 ELSE 0 
                END) as nowShowing,
                SUM(CASE 
                    WHEN release_date IS NULL OR release_date > CURDATE() 
                    THEN 1 ELSE 0 
                END) as comingSoon,
                ROUND(AVG(COALESCE(average_rating, 0)), 2) as avgRating
            FROM movies
        `;

        const [results] = await db.query(query);
        return results[0];
    }
}

module.exports = new MovieService();