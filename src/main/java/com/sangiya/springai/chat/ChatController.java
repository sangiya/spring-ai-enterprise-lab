package com.sangiya.springai.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ConversationMemory memory;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String content = chatService.chat(request.conversationId(), request.message());
        return new ChatResponse(content);
    }

    @DeleteMapping("/{conversationId}")
    public void clearHistory(@PathVariable String conversationId) {
        memory.clear(conversationId);
    }

    record ChatRequest(String conversationId, String message) {}
    record ChatResponse(String content) {}
}
