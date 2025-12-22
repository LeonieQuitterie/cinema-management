package com.cinema.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieDTO {
    private String id;
    private String title;
    private String description;
    private Integer duration;
    
    @JsonProperty("poster_url")
    private String posterUrl;
    
    @JsonProperty("release_date")
    private String releaseDate;
    
    private String language;
    
    @JsonProperty("age_rating")
    private String ageRating;
    
    @JsonProperty("age_rating_description")
    private String ageRatingDescription;
    
    @JsonProperty("average_rating")
    private String averageRating;
    
    @JsonProperty("total_ratings")
    private Integer totalRatings;
    
    // ✅ THÊM CÁC FIELD BỊ THIẾU
    @JsonProperty("trailer_url")
    private String trailerUrl;
    
    @JsonProperty("five_star")
    private Integer fiveStar;
    
    @JsonProperty("four_star")
    private Integer fourStar;
    
    @JsonProperty("three_star")
    private Integer threeStar;
    
    @JsonProperty("two_star")
    private Integer twoStar;
    
    @JsonProperty("one_star")
    private Integer oneStar;
    
    @JsonProperty("created_at")
    private String createdAt;

    // Constructors
    public MovieDTO() {}

    // ==================== GETTERS & SETTERS ====================
    
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
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

    public String getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(String averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Integer totalRatings) {
        this.totalRatings = totalRatings;
    }

    // ✅ GETTERS/SETTERS CHO CÁC FIELD MỚI
    
    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public Integer getFiveStar() {
        return fiveStar;
    }

    public void setFiveStar(Integer fiveStar) {
        this.fiveStar = fiveStar;
    }

    public Integer getFourStar() {
        return fourStar;
    }

    public void setFourStar(Integer fourStar) {
        this.fourStar = fourStar;
    }

    public Integer getThreeStar() {
        return threeStar;
    }

    public void setThreeStar(Integer threeStar) {
        this.threeStar = threeStar;
    }

    public Integer getTwoStar() {
        return twoStar;
    }

    public void setTwoStar(Integer twoStar) {
        this.twoStar = twoStar;
    }

    public Integer getOneStar() {
        return oneStar;
    }

    public void setOneStar(Integer oneStar) {
        this.oneStar = oneStar;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}