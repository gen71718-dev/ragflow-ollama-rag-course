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

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DifyChatClient implements AiChatClient {

    private final AiProperties properties;
    private final RestTemplate restTemplate;

    public DifyChatClient(AiProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public ChatResult chat(String query, String conversationId) {
        AiProperties.Dify dify = properties.getDify();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(dify.getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", Map.of());
        body.put("query", query);
        body.put("response_mode", "blocking");
        body.put("conversation_id", conversationId == null ? "" : conversationId);
        body.put("user", dify.getUser());

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                dify.getUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                JsonNode.class);

        JsonNode root = response.getBody();
        if (root == null) {
            throw new IllegalStateException("AI 平台返回空响应");
        }
        String answer = root.path("answer").asText("");
        String newConversationId = root.path("conversation_id").asText("");
        return new ChatResult(answer, newConversationId, "dify");
    }
}