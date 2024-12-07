package com.studentchat.chatservice;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        try {
            String notificationJson = objectMapper.writeValueAsString(new LikeNotification(
                    message.getId(), message.getUserId(), message.getLikes()));

            serviceBusSenderClient.sendMessage(new ServiceBusMessage(notificationJson));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification to Service Bus", e);
        }
    }

}
