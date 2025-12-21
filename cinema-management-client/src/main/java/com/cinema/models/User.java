package com.cinema.models;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User {

    private String id;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    /* =======================
       ENUM ROLE
       ======================= */
    public enum UserRole {
        ADMIN("Quản trị viên"),
        CINEMA_MANAGER("Quản lý rạp"),
        CUSTOMER("Khách hàng");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /* =======================
       CONSTRUCTORS
       ======================= */

    // Constructor rỗng (dùng cho ORM / Jackson)
    public User() {
        this.id = UUID.randomUUID().toString();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor cơ bản
    public User(String username, String passwordHash, UserRole role) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Constructor đầy đủ
    public User(String id,
                String username,
                String passwordHash,
                String email,
                String fullName,
                String phoneNumber,
                String avatarUrl,
                UserRole role,
                boolean isActive,
                LocalDateTime createdAt,
                LocalDateTime lastLogin) {

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    /* =======================
       GETTERS & SETTERS
       ======================= */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    /* =======================
       HELPER METHODS
       ======================= */

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isCinemaManager() {
        return role == UserRole.CINEMA_MANAGER;
    }

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    /* =======================
       OVERRIDES
       ======================= */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
