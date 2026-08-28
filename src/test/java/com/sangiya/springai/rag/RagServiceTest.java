package com.sangiya.springai.rag;

import com.sangiya.springai.client.OpenAiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private OpenAiClient client;

    private VectorStore vectorStore;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        vectorStore = new VectorStore();
        ragService = new RagService(vectorStore, client);
        // Stub embedding to return a fixed vector for any text
        when(client.embed(anyString()))
                .thenReturn(new float[]{0.1f, 0.2f, 0.3f});
    }

    @Test
    void query_returnsAnswerFromModel() {
        when(client.chatCompletion(anyList())).thenReturn("The answer is 42.");

        ragService.ingest(List.of("The answer to everything is 42."));
        String result = ragService.query("What is the answer?");

        assertThat(result).isEqualTo("The answer is 42.");
    }

    @Test
    void ingest_addsDocumentsToVectorStore() {
        ragService.ingest(List.of("passage one", "passage two", "passage three"));

        assertThat(vectorStore.size()).isEqualTo(3);
        verify(client, times(3)).embed(anyString());
    }

    @Test
    void query_includesRetrievedContextInPrompt() {
        ragService.ingest(List.of("Spring Boot 3 requires Java 17+."));
        when(client.chatCompletion(anyList())).thenReturn("Java 17 minimum.");

        ragService.query("What Java version?");

        // Verify the chat completion was called with messages containing context
        verify(client).chatCompletion(argThat(msgs ->
                msgs.stream().anyMatch(m -> m.get("content") != null
                        && m.get("content").contains("Spring Boot 3 requires Java 17+."))));
    }
}
