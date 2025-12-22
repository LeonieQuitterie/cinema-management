package com.cinema.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScreenDTO {
    private String id;
    private String name;
    
    @JsonProperty("cinema_id")
    private String cinemaId;
    
    @JsonProperty("row_count")
    private Integer rowCount;
    
    @JsonProperty("column_count")
    private Integer columnCount;
    
    @JsonProperty("total_seats")
    private Integer totalSeats;
    
    @JsonProperty("cinema_name")
    private String cinemaName;
    
    @JsonProperty("cinema_address")
    private String cinemaAddress;
    
    @JsonProperty("cinema_city")
    private String cinemaCity;
    
    @JsonProperty("cinema_logo_url")
    private String cinemaLogoUrl;

    // Constructors
    public ScreenDTO() {}

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getCinemaAddress() {
        return cinemaAddress;
    }

    public void setCinemaAddress(String cinemaAddress) {
        this.cinemaAddress = cinemaAddress;
    }

    public String getCinemaCity() {
        return cinemaCity;
    }

    public void setCinemaCity(String cinemaCity) {
        this.cinemaCity = cinemaCity;
    }

    public String getCinemaLogoUrl() {
        return cinemaLogoUrl;
    }

    public void setCinemaLogoUrl(String cinemaLogoUrl) {
        this.cinemaLogoUrl = cinemaLogoUrl;
    }
}