package com.studentchat.chatservice;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatMessageRepository;

    @Autowired
    private ServiceBusReceiverClient serviceBusReceiverClient;

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

    public List<String> getLikeNotificationsByUserId(String userId, int messageBatchSize) {

        IterableStream<ServiceBusReceivedMessage> peekedMessages = serviceBusReceiverClient.peekMessages(messageBatchSize);

        return peekedMessages.stream()
                .filter(message -> userId.equals(message.getApplicationProperties().get("userId")))
                .sorted(Comparator.comparing(ServiceBusReceivedMessage::getEnqueuedTime).reversed())
                .limit(5)
                .map(this::convertToLikeNotificationMessage)
                .collect(Collectors.toList());
    }

    private String convertToLikeNotificationMessage(ServiceBusReceivedMessage message) {

        String messageText = message.getBody().toString();
        int likes = (int) message.getApplicationProperties().getOrDefault("likes", 0);

        return String.format("Your message '%s' received %d likes", messageText, likes);
    }

}
