// src/services/movieService.js
const db = require("../config/database");

class MovieService {
    // src/services/movieService.js
    static async getAllMovies(filterStatus = null) {
        const today = new Date();
        today.setHours(0, 0, 0, 0); // Chỉ lấy ngày, bỏ giờ phút giây

        // Bước 1: Lấy tất cả showtimes từ hôm nay trở đi + thông tin phim
        const [rows] = await db.query(
            `
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
      s.start_time,
      s.end_time
    FROM showtimes s
    JOIN movies m ON s.movie_id = m.id
    WHERE DATE(s.start_time) >= DATE(?)
    ORDER BY m.release_date DESC, s.start_time
  `,
            [today]
        );

        if (rows.length === 0) return [];

        // Bước 2: Group theo movie_id và tính status
        const movieMap = new Map();

        const now = new Date();

        for (let row of rows) {
            const movieId = row.id;

            if (!movieMap.has(movieId)) {
                movieMap.set(movieId, {
                    id: row.id,
                    title: row.title,
                    description: row.description,
                    duration: row.duration,
                    poster_url: row.poster_url,
                    release_date: row.release_date,
                    language: row.language,
                    age_rating: row.age_rating,
                    age_rating_description: row.age_rating_description,
                    average_rating: row.average_rating,
                    total_ratings: row.total_ratings,
                    trailer_url: row.trailer_url,
                    status: null,
                    genres: [],
                });
            }

            // Tính status dựa vào suất chiếu
            const movie = movieMap.get(movieId);
            const startTime = new Date(row.start_time);
            const endTime = new Date(row.end_time);

            if (startTime <= now && endTime >= now) {
                movie.status = "NOW_SHOWING"; // ưu tiên cao nhất
            } else if (startTime > now && movie.status !== "NOW_SHOWING") {
                movie.status = "COMING_SOON";
            }
        }

        // Bước 3: Lấy genres cho các phim có suất chiếu
        const movieIds = Array.from(movieMap.keys());
        const [genreRows] = await db.query(
            `
    SELECT mg.movie_id, g.name
    FROM movie_genres mg
    JOIN genres g ON mg.genre_id = g.id
    WHERE mg.movie_id IN (?)
  `,
            [movieIds]
        );

        for (let gr of genreRows) {
            const movie = movieMap.get(gr.movie_id);
            if (movie) {
                movie.genres.push(gr.name);
            }
        }

        let movies = Array.from(movieMap.values());

        // Bước 4: Lọc theo status nếu client yêu cầu
        if (filterStatus) {
            movies = movies.filter((m) => m.status === filterStatus);
        }

        return movies;
    }

    // Lấy thống kê đánh giá sao cho 1 phim
    static async getRatingStats(movieId) {
        const [rows] = await db.query(
            `
      SELECT 
      average_rating,
        five_star,
        four_star,
        three_star,
        two_star,
        one_star,
        total_ratings
      FROM movies
      WHERE id = ?
    `,
            [movieId]
        );

        if (rows.length === 0) {
            throw new Error("Phim không tồn tại");
        }

        const stats = rows[0];
        const total = stats.total_ratings || 1; // tránh chia 0

        return {
            average_rating: stats.average_rating || 0,
            five_star: stats.five_star || 0,
            four_star: stats.four_star || 0,
            three_star: stats.three_star || 0,
            two_star: stats.two_star || 0,
            one_star: stats.one_star || 0,
            total_ratings: total,
            percentages: {
                five: (stats.five_star || 0) / total,
                four: (stats.four_star || 0) / total,
                three: (stats.three_star || 0) / total,
                two: (stats.two_star || 0) / total,
                one: (stats.one_star || 0) / total,
            },
            counts: {
                five: formatCount(stats.five_star || 0),
                four: formatCount(stats.four_star || 0),
                three: formatCount(stats.three_star || 0),
                two: formatCount(stats.two_star || 0),
                one: formatCount(stats.one_star || 0),
            },
        };
    }

    /**
     * Lấy danh sách diễn viên của một bộ phim theo movieId
     * @param {string} movieId
     * @returns {Promise<Array<{realName: string, characterName: string, imageUrl: string|null}>>}
     */
    static async getCastByMovieId(movieId) {
        const query = `
        SELECT 
            a.real_name AS realName,
            ma.character_name AS characterName,
            a.image_url AS imageUrl
            -- Nếu sau này cần bio: , a.bio AS bio
        FROM movie_actors ma
        JOIN actors a ON ma.actor_id = a.id
        WHERE ma.movie_id = ?
        ORDER BY ma.character_name ASC  -- Có thể thay bằng thứ tự ưu tiên nếu cần
    `;

        const [rows] = await db.query(query, [movieId]);
        return rows; // Trả về: [{ realName, characterName, imageUrl }, ...]
    }

    // Lấy comment + reaction của 1 phim
    static async getMovieComments(movieId) {
        const sql = `
            SELECT
                c.id              AS comment_id,
                c.user_id         AS user_id,
                u.full_name       AS user_name,
                u.avatar_url      AS user_avatar,
                c.rating,
                c.content,
                c.has_spoiler,
                c.created_at      AS comment_created_at,

                r.id              AS reaction_id,
                r.user_id         AS reaction_user_id,
                r.reaction_type,
                r.created_at      AS reaction_created_at

            FROM comments c
            JOIN users u ON u.id = c.user_id
            LEFT JOIN comment_reactions r ON r.comment_id = c.id
            WHERE c.movie_id = ?
            ORDER BY c.created_at DESC, r.created_at ASC
        `;

        const [rows] = await db.execute(sql, [movieId]);

        // ===== GROUP COMMENT + REACTIONS =====
        const commentMap = {};

        for (const row of rows) {
            if (!commentMap[row.comment_id]) {
                commentMap[row.comment_id] = {
                    id: row.comment_id,
                    userId: row.user_id,
                    userName: row.user_name,
                    userAvatar: row.user_avatar,
                    rating: row.rating,
                    content: row.content,
                    hasSpoiler: !!row.has_spoiler,
                    createdAt: row.comment_created_at,
                    reactionsList: [],
                };
            }

            if (row.reaction_id) {
                commentMap[row.comment_id].reactionsList.push({
                    id: row.reaction_id,
                    commentId: row.comment_id,
                    userId: row.reaction_user_id,
                    reactionType: row.reaction_type,
                    createdAt: row.reaction_created_at,
                });
            }
        }

        return Object.values(commentMap);
    }
}

// Hàm format số lượng kiểu "11.2K", "4.6K"
function formatCount(count) {
    if (count >= 1000) {
        return (count / 1000).toFixed(1) + "K";
    }
    return count.toString();
}

module.exports = MovieService;
