package com.sangiya.springai.chat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    @Test
    void chat_returnsContentFromModel() {
        ChatModel model = mock(ChatModel.class);
        ChatResponse response = new ChatResponse(
                java.util.List.of(new Generation(new AssistantMessage("Hello from AI!"))));
        when(model.call(any(Prompt.class))).thenReturn(response);

        ChatClient.Builder builder = ChatClient.builder(model);
        ChatService service = new ChatService(builder);

        String result = service.chat("conv-1", "Hi!");

        assertThat(result).isEqualTo("Hello from AI!");
    }
}
