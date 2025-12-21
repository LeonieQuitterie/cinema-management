package com.cinema.models;

import java.time.LocalDateTime;

public class CommentReaction {
    private String id;
    private String commentId;
    private String userId;
    private String reactionType; // 'like', 'love', 'haha', 'wow', 'sad', 'angry'
    private LocalDateTime createdAt;
    
    public CommentReaction() {}
    
    public CommentReaction(String commentId, String userId, String reactionType) {
        this.commentId = commentId;
        this.userId = userId;
        this.reactionType = reactionType;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}