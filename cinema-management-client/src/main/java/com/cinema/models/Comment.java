package com.cinema.models;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Comment {
    private String id;
    private String userId;
    private String userName;        // ⭐ THÊM - để hiển thị
    private String userAvatar;      // ⭐ THÊM - để hiển thị
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private boolean hasSpoiler = false;

    // ⭐ SỬA - Thay Map thành List để track từng user
    private List<CommentReaction> reactionsList = new ArrayList<>();

    // Constructor
    public Comment() {}

    public Comment(String userId, int rating, String content) {
        this.userId = userId;
        this.rating = rating;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // === GETTERS & SETTERS - GIỮ NGUYÊN ===
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // ⭐ THÊM - Getters/Setters mới
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isHasSpoiler() { return hasSpoiler; }
    public void setHasSpoiler(boolean hasSpoiler) { this.hasSpoiler = hasSpoiler; }

    // ❌ XÓA - Không cần parentId nữa
    // public String getParentId() { return parentId; }
    // public void setParentId(String parentId) { this.parentId = parentId; }

    // === REACTIONS - SỬA ===
    
    public List<CommentReaction> getReactionsList() { 
        return reactionsList; 
    }
    
    public void setReactionsList(List<CommentReaction> reactionsList) { 
        this.reactionsList = reactionsList != null ? reactionsList : new ArrayList<>(); 
    }

    // ⭐ SỬA - Các helper methods để làm việc với reactions
    
    // Đếm số lượng từng loại reaction (trả về Map để UI dễ dùng)
    public Map<String, Integer> getReactions() {
        Map<String, Integer> counts = new HashMap<>();
        for (CommentReaction reaction : reactionsList) {
            counts.merge(reaction.getReactionType(), 1, Integer::sum);
        }
        return counts;
    }
    
    // ❌ XÓA - Không cần addReaction/removeReaction kiểu này nữa
    // public void addReaction(String type) { ... }
    // public void removeReaction(String type) { ... }

    // GIỮ NGUYÊN - Tổng số reactions
    public int getTotalReactions() {
        return reactionsList.size();
    }

    // GIỮ NGUYÊN - Top 3 reactions
    public List<String> getTop3Reactions() {
        return getReactions().entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    // ⭐ THÊM - Kiểm tra user đã react gì chưa
    public String getUserReaction(String userId) {
        return reactionsList.stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(CommentReaction::getReactionType)
                .findFirst()
                .orElse(null);
    }

    // ❌ XÓA - Không cần replies nữa
    // public List<Comment> getReplies() { ... }
    // public void setReplies(List<Comment> replies) { ... }
    // public int getReplyCount() { ... }

    // === GIỮ NGUYÊN - Thời gian đẹp như Facebook ===
    public String getFormattedTime() {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.temporal.ChronoUnit.SECONDS.between(createdAt, now);

        if (seconds < 60) return "vừa xong";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " phút";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ";
        long days = hours / 24;
        if (days < 7) return days + " ngày";
        long weeks = days / 7;
        if (weeks < 4) return weeks + " tuần";

        return createdAt.toLocalDate().toString();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                ", reactions=" + getTotalReactions() +
                '}';
    }
}