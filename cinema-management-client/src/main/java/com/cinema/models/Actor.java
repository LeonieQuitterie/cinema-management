package com.cinema.models;

import com.google.gson.annotations.SerializedName;

public class Actor {
    private String id;
    private String realName;

    @SerializedName("characterName")  // ← Quan trọng: map từ JSON "characterName" của backend
    private String character;         // ← Giữ tên field là character để admin code không lỗi

    private String imageUrl;
    private String bio;

    public Actor() {}

    public Actor(String id, String realName, String character, String imageUrl) {
        this.id = id;
        this.realName = realName;
        this.character = character;
        this.imageUrl = imageUrl;
    }

    // Getters & Setters – giữ nguyên tên cũ để admin code hoạt động
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}