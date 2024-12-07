package com.studentchat.chatservice;

public class LikeNotification {
    private String messageId;
    private String userId;
    private int likes;

    public LikeNotification(String messageId, String userId, int likes) {
        this.messageId = messageId;
        this.userId = userId;
        this.likes = likes;
    }

}