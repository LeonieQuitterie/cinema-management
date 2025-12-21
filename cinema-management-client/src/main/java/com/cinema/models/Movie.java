package com.cinema.models;

import java.time.LocalDate;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Movie {
    private String id;
    private String title;
    private String description;
    private int duration; // minutes
    private double rating;

    @SerializedName("poster_url")
    private String posterUrl;

    private List<String> genres;

    @SerializedName("release_date")
    private LocalDate releaseDate; // ngày phát hành

    private MovieStatus status;
    private String language;

    @SerializedName("age_rating")
    private String ageRating; // C18, C16, P, K

    @SerializedName("age_rating_description")
    private String ageRatingDescription;

    @SerializedName("average_rating")
    private double averageRating;

    @SerializedName("total_ratings")
    private int totalRatings;
    private List<Actor> actors;
    private List<Comment> comments;
    private RatingBreakdown ratingBreakdown;

    @SerializedName("trailer_url")
    private String trailerUrl;

    public enum MovieStatus {
        NOW_SHOWING("Đang chiếu"),
        COMING_SOON("Sắp chiếu");

        private final String displayName;

        MovieStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    
// Default constructor
public Movie() {
}

    // Constructor
    public Movie(String id,
            String title,
            String description,
            int duration,
            double rating,
            String posterUrl,
            List<String> genres,
            LocalDate releaseDate,
            MovieStatus status,
            String language,
            String ageRating,
            String ageRatingDescription,
            double averageRating,
            int totalRatings,
            List<Actor> actors,
            List<Comment> comments,
            RatingBreakdown ratingBreakdown,
            String trailerUrl) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.genres = genres;
        this.releaseDate = releaseDate;
        this.status = status;
        this.language = language;
        this.ageRating = ageRating;
        this.ageRatingDescription = ageRatingDescription;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
        this.actors = actors;
        this.comments = comments;
        this.ratingBreakdown = ratingBreakdown;
        this.trailerUrl = trailerUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public MovieStatus getStatus() {
        return status;
    }

    public void setStatus(MovieStatus status) {
        this.status = status;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getAgeRatingDescription() {
        return ageRatingDescription;
    }

    public void setAgeRatingDescription(String ageRatingDescription) {
        this.ageRatingDescription = ageRatingDescription;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public RatingBreakdown getRatingBreakdown() {
        return ratingBreakdown;
    }

    public void setRatingBreakdown(RatingBreakdown ratingBreakdown) {
        this.ratingBreakdown = ratingBreakdown;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getDurationFormatted() {
        return duration + " phút";
    }

    public String getRatingFormatted() {
        return String.format("%.1f/10", rating);
    }
}