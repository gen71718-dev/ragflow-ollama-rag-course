package com.example.ragdemo.service;

import com.example.ragdemo.client.AiChatClient;
import com.example.ragdemo.client.ChatResult;
import com.example.ragdemo.client.DifyChatClient;
import com.example.ragdemo.client.RagflowChatClient;
import com.example.ragdemo.config.AiProperties;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final AiChatClient chatClient;

    public ChatService(AiProperties properties, DifyChatClient difyChatClient, RagflowChatClient ragflowChatClient) {
        this.chatClient = "ragflow".equalsIgnoreCase(properties.getPlatform()) ? ragflowChatClient : difyChatClient;
    }

    public ChatResult chat(String query, String conversationId) {
        return chatClient.chat(query, conversationId);
    }
}