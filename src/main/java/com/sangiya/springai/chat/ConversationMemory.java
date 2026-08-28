package com.sangiya.springai.chat;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory conversation store.
 * Each conversation ID maps to its ordered message history.
 * Thread-safe for concurrent requests on different conversation IDs.
 */
@Component
public class ConversationMemory {

    private final Map<String, List<Map<String, String>>> history = new ConcurrentHashMap<>();

    public List<Map<String, String>> getHistory(String conversationId) {
        return history.computeIfAbsent(conversationId, id -> new ArrayList<>());
    }

    public void addUserMessage(String conversationId, String content) {
        getHistory(conversationId).add(Map.of("role", "user", "content", content));
    }

    public void addAssistantMessage(String conversationId, String content) {
        getHistory(conversationId).add(Map.of("role", "assistant", "content", content));
    }

    public void clear(String conversationId) {
        history.remove(conversationId);
    }

    public int messageCount(String conversationId) {
        return getHistory(conversationId).size();
    }
}
