package com.sangiya.springai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper over the OpenAI Chat Completions REST API using
 * Java's built-in HttpClient (no third-party HTTP library needed).
 *
 * All patterns in this project (chat, RAG, function calling) build on
 * top of this single class, demonstrating the raw API before introducing
 * a framework abstraction like Spring AI.
 */
@Component
@Slf4j
public class OpenAiClient {

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public OpenAiClient(
            ObjectMapper mapper,
            @Value("${ai.openai.api-key:demo-key}") String apiKey,
            @Value("${ai.openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${ai.openai.model:gpt-4o-mini}") String model) {
        this.http = HttpClient.newHttpClient();
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    /**
     * Sends a list of messages to the Chat Completions endpoint and returns
     * the assistant's reply as a plain string.
     */
    public String chatCompletion(List<Map<String, String>> messages) {
        try {
            var body = mapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", messages
            ));

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("OpenAI status={} body={}", response.statusCode(), response.body());

            JsonNode root = mapper.readTree(response.body());
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new AiClientException("OpenAI chat completion failed", e);
        }
    }

    /**
     * Calls the Embeddings endpoint and returns a float array suitable
     * for cosine-similarity calculations.
     */
    public float[] embed(String text) {
        try {
            var body = mapper.writeValueAsString(Map.of(
                    "model", "text-embedding-3-small",
                    "input", text
            ));

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/embeddings"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            JsonNode embeddingNode = root.path("data").path(0).path("embedding");

            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = (float) embeddingNode.path(i).asDouble();
            }
            return embedding;
        } catch (Exception e) {
            log.error("OpenAI embedding failed: {}", e.getMessage(), e);
            throw new AiClientException("OpenAI embedding failed", e);
        }
    }
}
