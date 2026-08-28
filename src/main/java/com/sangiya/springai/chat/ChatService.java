package com.sangiya.springai.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Chat service demonstrating:
 *  1. Basic prompt → completion round-trip
 *  2. Streaming completions via Flux for server-sent events
 *  3. Conversation memory — each conversation ID maintains its own message history
 */
@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are a helpful enterprise software assistant.")
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    /** Blocking single-turn completion. */
    public String chat(String conversationId, String userMessage) {
        log.info("Chat request conversationId={} message={}", conversationId, userMessage);
        return chatClient.prompt()
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CONVERSATION_ID_KEY, conversationId))
                .user(userMessage)
                .call()
                .content();
    }

    /** Streaming completion — each token is emitted as it arrives from the model. */
    public Flux<String> stream(String conversationId, String userMessage) {
        return chatClient.prompt()
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CONVERSATION_ID_KEY, conversationId))
                .user(userMessage)
                .stream()
                .content();
    }
}
