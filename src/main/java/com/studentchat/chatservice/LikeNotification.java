package com.studentchat.chatservice;

public class LikeNotification {
    private String messageId;
    private String userId;
    private String content;
    private int likes;

    public LikeNotification(String messageId, String userId, int likes, String content) {
        this.messageId = messageId;
        this.userId = userId;
        this.likes = likes;
        this.content = content;
    }

    public LikeNotification() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}