package com.cinema.models.dto;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    // Constructors
    public ApiResponse() {}

    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    // Getters & Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}