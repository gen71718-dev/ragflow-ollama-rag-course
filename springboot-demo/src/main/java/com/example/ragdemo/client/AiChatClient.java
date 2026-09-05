package com.example.ragdemo.client;

public interface AiChatClient {

    ChatResult chat(String query, String conversationId);
}