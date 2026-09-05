package com.example.ragdemo.client;

import com.example.ragdemo.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class RagflowChatClient implements AiChatClient {

    private final AiProperties properties;
    private final RestTemplate restTemplate;

    public RagflowChatClient(AiProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public ChatResult chat(String query, String conversationId) {
        AiProperties.Ragflow ragflow = properties.getRagflow();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ragflow.getApiKey());

        Map<String, Object> body = Map.of(
                "model", ragflow.getModel(),
                "messages", List.of(Map.of("role", "user", "content", query)));

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                ragflow.getUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                JsonNode.class);

        JsonNode root = response.getBody();
        if (root == null) {
            throw new IllegalStateException("AI 平台返回空响应");
        }
        String answer = root.path("choices").path(0).path("message").path("content").asText("");
        return new ChatResult(answer, "", "ragflow");
    }
}