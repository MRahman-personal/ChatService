package com.studentchat.chatservice;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatMessageRepository;

    @Autowired
    private ServiceBusSenderClient serviceBusSenderClient;

    public List<ChatMessage> getAllMessages() {
        return StreamSupport
                .stream(chatMessageRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    public ChatMessage incrementLikes(String messageId) {
        return chatMessageRepository.findById(messageId).map(chatMessage -> {
            chatMessage.setLikes(chatMessage.getLikes() + 1);
            ChatMessage updatedMessage = chatMessageRepository.save(chatMessage);

            sendLikeNotificationToServiceBus(updatedMessage);
            return updatedMessage;
        }).orElseThrow(() -> new RuntimeException("Message not found"));
    }

    private void sendLikeNotificationToServiceBus(ChatMessage message) {
        // todo update message format
        String notification = String.format("Message ID %s User %s Likes %d",
                message.getId(), message.getUserId(), message.getLikes());

        serviceBusSenderClient.sendMessage(new ServiceBusMessage(notification));
    }
}
