package com.studentchat.chatservice;

import org.springframework.stereotype.Repository;
import com.azure.spring.data.cosmos.repository.CosmosRepository;

@Repository
public interface ChatRepository extends CosmosRepository<ChatMessage, String> {
}

