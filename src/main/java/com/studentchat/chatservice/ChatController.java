package com.studentchat.chatservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/messages")
    public List<ChatMessage> getAllMessages() {
        return chatService.getAllMessages();
    }

    @GetMapping("/notifications/{id}")
    public List<String> getNotifications(@PathVariable String id) {
        return chatService.getLikeNotificationsByUserId(id, 100);
    }

    @PostMapping("/send")
    public ChatMessage sendMessage(@RequestBody ChatMessage message) {
        return chatService.saveMessage(message);
    }

    @PostMapping("/like/{id}")
    public ChatMessage likeMessage(@PathVariable String id) {
        return chatService.incrementLikes(id);
    }

}