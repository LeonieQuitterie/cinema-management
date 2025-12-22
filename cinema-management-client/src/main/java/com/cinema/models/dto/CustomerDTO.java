package com.cinema.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerDTO {
    private String id;
    
    @JsonProperty("fullName")
    private String fullName;
    
    @JsonProperty("phoneNumber")
    private String phoneNumber;
    
    private String email;

    // Constructors
    public CustomerDTO() {}

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}