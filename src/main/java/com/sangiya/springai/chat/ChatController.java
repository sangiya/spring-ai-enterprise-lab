package com.sangiya.springai.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String content = chatService.chat(request.conversationId(), request.message());
        return new ChatResponse(content);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ChatRequest request) {
        return chatService.stream(request.conversationId(), request.message());
    }

    record ChatRequest(String conversationId, String message) {}
    record ChatResponse(String content) {}
}
