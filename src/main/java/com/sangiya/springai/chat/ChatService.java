package com.sangiya.springai.chat;

import com.sangiya.springai.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chat service with stateful conversation memory.
 *
 * Each call to chat() appends the user message and assistant reply to the
 * conversation history for that conversationId. Subsequent calls include
 * the full history so the model has context of prior turns — exactly how
 * production chatbots maintain multi-turn state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private static final String SYSTEM_PROMPT =
            "You are a helpful enterprise software assistant. Be concise and precise.";

    private final OpenAiClient client;
    private final ConversationMemory memory;

    public String chat(String conversationId, String userMessage) {
        log.info("Chat conversationId={} message={}", conversationId, userMessage);

        memory.addUserMessage(conversationId, userMessage);

        List<Map<String, String>> messages = buildMessages(conversationId);
        String reply = client.chatCompletion(messages);

        memory.addAssistantMessage(conversationId, reply);
        return reply;
    }

    private List<Map<String, String>> buildMessages(String conversationId) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        messages.addAll(memory.getHistory(conversationId));
        return messages;
    }
}
