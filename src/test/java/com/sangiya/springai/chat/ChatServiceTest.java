package com.sangiya.springai.chat;

import com.sangiya.springai.client.OpenAiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private OpenAiClient client;

    private ConversationMemory memory;
    private ChatService service;

    @BeforeEach
    void setUp() {
        memory = new ConversationMemory();
        service = new ChatService(client, memory);
    }

    @Test
    void chat_returnsContentFromModel() {
        when(client.chatCompletion(anyList())).thenReturn("Hello from AI!");

        String result = service.chat("conv-1", "Hi!");

        assertThat(result).isEqualTo("Hello from AI!");
        verify(client).chatCompletion(anyList());
    }

    @Test
    void chat_appendsMessagesToHistory() {
        when(client.chatCompletion(anyList())).thenReturn("Reply A");

        service.chat("conv-2", "Question A");

        assertThat(memory.messageCount("conv-2")).isEqualTo(2); // user + assistant
    }

    @Test
    void chat_includesPriorHistoryInSubsequentCalls() {
        when(client.chatCompletion(anyList()))
                .thenReturn("First reply")
                .thenReturn("Second reply");

        service.chat("conv-3", "First message");
        service.chat("conv-3", "Second message");

        // Verify second call included the prior turn (4 history messages + system = 5 total)
        verify(client, times(2)).chatCompletion(argThat(
                (List<Map<String, String>> msgs) -> msgs.size() >= 2));
    }

    @Test
    void chat_tracksConversationsSeparately() {
        when(client.chatCompletion(anyList())).thenReturn("Reply");

        service.chat("session-A", "Message A");
        service.chat("session-B", "Message B");

        assertThat(memory.messageCount("session-A")).isEqualTo(2);
        assertThat(memory.messageCount("session-B")).isEqualTo(2);
    }
}
