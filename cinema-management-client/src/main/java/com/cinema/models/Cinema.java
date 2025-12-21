package com.cinema.models;

import java.util.ArrayList;
import java.util.List;

public class Cinema {
    private String id;
    private String name;
    private String address;
    private String city;
    private String logoUrl;
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

    // Getters and Setters
    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public void setScreens(List<Screen> screens) {
        this.screens = screens;
    }

    public void addScreen(Screen screen) {
        this.screens.add(screen);
    }

    // ✅ THÊM 2 METHODS NÀY

    /**
     * Lấy số lượng phòng chiếu
     * Dùng cho TableView hiển thị số phòng
     */
    public int getScreenCount() {
        return screens != null ? screens.size() : 0;
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