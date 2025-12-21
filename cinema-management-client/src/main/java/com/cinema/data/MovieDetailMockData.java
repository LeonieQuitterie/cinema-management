package com.cinema.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cung cấp dữ liệu mock để test trang Movie Detail
 * Sau này sẽ thay thế bằng API calls từ backend
 * 
 * Cấu trúc:
 * 1. Inner classes định nghĩa dữ liệu (Movie, Actor, Comment, etc.)
 * 2. Static methods trả về mock data
 * 3. Constructor để dễ tạo objects
 */
public class MovieDetailMockData {

    /**
     * Mock Movie Object
     * Tương ứng với Entity Movie từ backend
     */
    public static class DuneMockMovie {
        public String id;
        public String title;
        public String titleOriginal;
        public String posterUrl;
        public String trailerUrl;
        
        public String synopsis;           // Mô tả ngắn (3-4 dòng)
        public String fullSynopsis;       // Mô tả đầy đủ
        
        public List<String> genres;
        public LocalDate releaseDate;
        public int duration;              // Tính theo phút
        public String ageRating;          // "18+", "16+", "P", "K"
        public String ageRatingDescription;
        public String language;           // "Phụ đề + Lồng tiếng"
        
        public double averageRating;
        public int totalRatings;
        
        // Rating Breakdown (cho 5 mức sao)
        public int fiveStar;
        public int fourStar;
        public int threeStar;
        public int twoStar;
        public int oneStar;
        
        public List<ActorMock> actors;
        public List<CommentMock> comments;
        public List<NewsMock> news;
        public List<QAMock> qa;

        public DuneMockMovie() {
            // Constructor rỗng
        }

        // Getters (nếu dùng Object mapping)
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getPosterUrl() { return posterUrl; }
        public double getAverageRating() { return averageRating; }
    }

    /**
     * Mock Actor Object
     */
    public static class ActorMock {
        public String id;
        public String realName;
        public String character;
        public String imageUrl;
        public String bio;

        public ActorMock(String id, String realName, String character, String imageUrl) {
            this.id = id;
            this.realName = realName;
            this.character = character;
            this.imageUrl = imageUrl;
        }

        public ActorMock(String id, String realName, String character, String imageUrl, String bio) {
            this(id, realName, character, imageUrl);
            this.bio = bio;
        }
    }

    /**
     * Mock Comment Object
     */
    public static class CommentMock {
        public String id;
        public String userId;
        public String username;
        public String userAvatarUrl;
        public int rating;                // 1-5 sao
        public String content;
        public String createdAt;          // ISO format: "2024-02-28T20:00:00"
        public int likes;
        public int dislikes;
        public boolean hasSpoiler;
        public int replyCount;

        public CommentMock(String id, String userId, String username, String userAvatarUrl,
                          int rating, String content, String createdAt, int likes,
                          int dislikes, boolean hasSpoiler, int replyCount) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.userAvatarUrl = userAvatarUrl;
            this.rating = rating;
            this.content = content;
            this.createdAt = createdAt;
            this.likes = likes;
            this.dislikes = dislikes;
            this.hasSpoiler = hasSpoiler;
            this.replyCount = replyCount;
        }
    }

    /**
     * Mock News Object
     */
    public static class NewsMock {
        public String id;
        public String title;
        public String source;
        public String description;
        public String imageUrl;
        public String publishedAt;
        public String url;

        public NewsMock(String id, String title, String source, String description,
                       String imageUrl, String publishedAt, String url) {
            this.id = id;
            this.title = title;
            this.source = source;
            this.description = description;
            this.imageUrl = imageUrl;
            this.publishedAt = publishedAt;
            this.url = url;
        }
    }

    /**
     * Mock Q&A Object
     */
    public static class QAMock {
        public String id;
        public String question;
        public List<String> tags;
        public int answerCount;
        public int views;
        public int upvotes;
        public int downvotes;

        public QAMock(String id, String question, List<String> tags,
                     int answerCount, int views, int upvotes, int downvotes) {
            this.id = id;
            this.question = question;
            this.tags = tags;
            this.answerCount = answerCount;
            this.views = views;
            this.upvotes = upvotes;
            this.downvotes = downvotes;
        }
    }

    /**
     * ============ STATIC METHODS - TRẢ VỀ MOCK DATA ============
     */

    /**
     * Lấy mock data cho phim Dune: Part Two
     * 
     * @return DuneMockMovie object
     */
    public static DuneMockMovie getDuneMockMovie() {
        DuneMockMovie movie = new DuneMockMovie();
        
        // Basic Info
        movie.id = "movie-dune-2024";
        movie.title = "Dune: Part Two";
        movie.titleOriginal = "Dune: Part Two (2024)";
        movie.posterUrl = "/images/movies/dune-part-two.jpg";
        movie.trailerUrl = "https://www.youtube.com/embed/n9xhJrCmwtc";
        
        // Synopsis
        movie.synopsis = "Paul Atreides, người con trai của một gia đình quý tộc đã được giao nhiệm vụ chuyên chở những ngôi sao quý giá trên hành tinh Arrakis, một sa mạc rộng lớn và đầy nguy hiểm, là điểm đến duy nhất trong vũ trụ nơi sản xuất ra chất dầu mô-tơ bí ẩn nhất quý giá nhất...";
        
        movie.fullSynopsis = "Paul Atreides, người con trai của một gia đình quý tộc đã được giao nhiệm vụ chuyên chở những ngôi sao quý giá trên hành tinh Arrakis, một sa mạc rộng lớn và đầy nguy hiểm, là điểm đến duy nhất trong vũ trụ nơi sản xuất ra chất dầu mô-tơ bí ẩn nhất quý giá nhất. Tuy nhiên, sau khi bước chân lên hành tinh này, Paul phát hiện ra rằng anh ta không chỉ là một nhân vật bình thường mà còn có một sức mạnh bí ẩn sâu sắc. Anh ta phải vật lộn để tìm hiểu bản thân mình, vượt qua những thách thức chết người, và xác định vị trí của mình trong vũ trụ. Cuộc hành trình của Paul sẽ định hình tương lai của toàn bộ vũ trụ.";
        
        // Genres
        movie.genres = Arrays.asList("Action", "Sci-Fi", "Adventure");
        
        // Details
        movie.releaseDate = LocalDate.of(2024, 2, 27);
        movie.duration = 166;  // 2 giờ 46 phút
        movie.ageRating = "18+";
        movie.ageRatingDescription = "Phim được phổ biến đến người xem từ đủ 18 trở lên";
        movie.language = "Phụ đề + Lồng tiếng";
        
        // Rating
        movie.averageRating = 8.5;
        movie.totalRatings = 19300;
        
        // Rating Breakdown
        movie.fiveStar = 11200;    // 58%
        movie.fourStar = 4600;     // 24%
        movie.threeStar = 2100;    // 11%
        movie.twoStar = 800;       // 4%
        movie.oneStar = 600;       // 3%
        
        // Actors
        movie.actors = getDuneMockActors();
        
        // Comments
        movie.comments = getDuneMockComments();
        
        // News
        movie.news = getDuneMockNews();
        
        // Q&A
        movie.qa = getDuneMockQA();
        
        return movie;
    }

    /**
     * Mock danh sách diễn viên Dune
     */
    private static List<ActorMock> getDuneMockActors() {
        List<ActorMock> actors = new ArrayList<>();
        actors.add(new ActorMock("1", "Timothée Chalamet", "Paul Atreides", "/images/actors/timothee.jpg"));
        actors.add(new ActorMock("2", "Zendaya", "Chani", "/images/actors/zendaya.jpg"));
        actors.add(new ActorMock("3", "Rebecca Ferguson", "Lady Jessica", "/images/actors/rebecca.jpg"));
        actors.add(new ActorMock("4", "Oscar Isaac", "Duke Leto", "/images/actors/oscar.jpg"));
        actors.add(new ActorMock("5", "Austin Butler", "Feyd-Rautha", "/images/actors/austin.jpg"));
        actors.add(new ActorMock("6", "Florence Pugh", "Princess Irulan", "/images/actors/florence.jpg"));
        return actors;
    }

    /**
     * Mock bình luận
     */
    private static List<CommentMock> getDuneMockComments() {
        List<CommentMock> comments = new ArrayList<>();
        
        comments.add(new CommentMock(
            "c1", "u1", "Nguyễn Thị Liên", "/images/avatars/user1.jpg",
            5,
            "Một bộ phim tuyệt vời! Hình ảnh đẹp mắt, âm thanh sư phạm, và cốt truyện hấp dẫn. Timothée Chalamet diễn xuất tuyệt vời trong vai Paul. Tôi chắc chắn sẽ xem lại bộ phim này!",
            "2024-02-28T20:00:00",
            99, 2, false, 3
        ));
        
        comments.add(new CommentMock(
            "c2", "u2", "Trần Minh Quân", "/images/avatars/user2.jpg",
            4,
            "Bộ phim hay nhưng hơi dài. Nếu cắt giảm 20 phút sẽ tốt hơn. Nhưng nhìn chung, Dune 2 vẫn đáng xem vì cinematography và visual effects.",
            "2024-02-27T18:30:00",
            45, 1, true, 1
        ));
        
        comments.add(new CommentMock(
            "c3", "u3", "Lê Hoàng Anh", "/images/avatars/user3.jpg",
            3,
            "Cảnh chiến đấu rất ấn tượng nhưng cốt truyện có chút khó hiểu. Nên xem phần 1 trước.",
            "2024-02-26T14:45:00",
            32, 0, false, 0
        ));
        
        return comments;
    }

    /**
     * Mock tin tức
     */
    private static List<NewsMock> getDuneMockNews() {
        List<NewsMock> news = new ArrayList<>();
        
        news.add(new NewsMock(
            "n1",
            "Phỏng vấn Timothée Chalamet về vai diễn Paul Atreides",
            "Deadline Hollywood",
            "Diễn viên trẻ chia sẻ về những thách thức khi đóng vai nhân vật phức tạp Paul Atreides và hành trình của anh ta trong Dune: Part Two...",
            "/images/news/dune-news1.jpg",
            "2024-02-25T10:00:00",
            "https://deadline.com/..."
        ));
        
        news.add(new NewsMock(
            "n2",
            "Dune: Part Two vượt mốc 700 triệu đô la toàn cầu",
            "The Hollywood Reporter",
            "Bom tấn khoa học viễn tưởng đạt thành công vượt mong đợi tại các phòng vé trên toàn thế giới...",
            "/images/news/dune-news2.jpg",
            "2024-02-20T14:30:00",
            "https://hollywoodreporter.com/..."
        ));
        
        return news;
    }

    /**
     * Mock Q&A
     */
    private static List<QAMock> getDuneMockQA() {
        List<QAMock> qa = new ArrayList<>();
        
        qa.add(new QAMock(
            "q1",
            "Có spoiler trong Dune: Part Two không?",
            Arrays.asList("spoiler", "nội-dung"),
            12, 450, 120, 5
        ));
        
        qa.add(new QAMock(
            "q2",
            "Cốt phim kết thúc như thế nào?",
            Arrays.asList("spoiler", "kết-cục"),
            8, 320, 85, 3
        ));
        
        qa.add(new QAMock(
            "q3",
            "Phần 3 sẽ ra mắt khi nào?",
            Arrays.asList("phần-tiếp"),
            5, 280, 60, 2
        ));
        
        return qa;
    }

    /**
     * TODO: HÀNG CHỜ CÓ THỂ LẤY THÊM
     * 
     * public static List<Movie> getAllMovies() { ... }
     * public static Movie getMovieById(String id) { ... }
     * public static List<Comment> getCommentsByMovieId(String movieId) { ... }
     * public static RatingBreakdown getRatingBreakdown(String movieId) { ... }
     */
}