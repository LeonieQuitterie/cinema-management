package com.cinema.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Cinema {
    private String id;
    private String name;
    private String address;
    private String city;

    @SerializedName("logo_url")
    private String logoUrl;

    @SerializedName("screenCount")
    private int screenCount;

    private List<Screen> screens;

    public Cinema() {
        this.screens = new ArrayList<>();
    }

    public Cinema(String id, String name, String address, String city) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.screens = new ArrayList<>();
    }

    public Cinema(String name, String city, String address, Integer screenCount) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.screenCount = screenCount;
        this.screens = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    
    public int getScreenCount() { 
        return screenCount > 0
            ? screenCount
            : (screens != null ? screens.size() : 0);
    }
    public void setScreenCount(Integer screenCount) { this.screenCount = screenCount; }
    
    public List<Screen> getScreens() { 
        return screens != null ? screens : new ArrayList<>();
    }
    public void setScreens(List<Screen> screens) { this.screens = screens; }
    
    @Override
    public String toString() {
        return name;
    }

    public void addScreen(Screen screen) {
        this.screens.add(screen);
    }

    /**
     * Constructor tiện lợi để tạo mock data
     */
    public Cinema(String name, String city, String address, int screenCount) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.screens = new ArrayList<>();

        // Tạo mock screens
        for (int i = 1; i <= screenCount; i++) {
            Screen screen = new Screen();
            screen.setId("screen_" + i);
            screen.setName("Phòng " + i); // ✅ CHỈ DÙNG setName() - Phòng 1, Phòng 2, Phòng 3...
            this.screens.add(screen);
        }
    }
}