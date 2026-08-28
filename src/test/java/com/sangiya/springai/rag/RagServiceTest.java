package com.sangiya.springai.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RagServiceTest {

    @Test
    void query_returnsAnswerFromModel() {
        ChatModel chatModel = mock(ChatModel.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);

        // Stub embedding model to return a fixed vector for any input
        when(embeddingModel.embed(any(org.springframework.ai.document.Document.class)))
                .thenReturn(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingModel.embed(any(String.class)))
                .thenReturn(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingModel.dimensions()).thenReturn(3);

        ChatResponse response = new ChatResponse(
                List.of(new Generation(new AssistantMessage("The answer is 42."))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);

        var vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        RagService service = new RagService(builder, vectorStore);

        service.ingest(List.of("The answer to everything is 42."));
        String result = service.query("What is the answer?");

        assertThat(result).isEqualTo("The answer is 42.");
    }
}
