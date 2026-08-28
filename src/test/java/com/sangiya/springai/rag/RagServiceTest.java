package com.sangiya.springai.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RagServiceTest {

    @Test
    void query_returnsAnswerFromModel() {
        ChatModel chatModel = mock(ChatModel.class);
        VectorStore vectorStore = mock(VectorStore.class);

        // Vector store returns no documents (empty context is still valid)
        when(vectorStore.similaritySearch(any())).thenReturn(List.of());

        ChatResponse response = new ChatResponse(
                List.of(new Generation(new AssistantMessage("The answer is 42."))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);

        ChatClient.Builder builder = ChatClient.builder(chatModel);
        RagService service = new RagService(builder, vectorStore);

        String result = service.query("What is the answer?");

        assertThat(result).isEqualTo("The answer is 42.");
    }

    @Test
    void ingest_delegatesToVectorStore() {
        ChatModel chatModel = mock(ChatModel.class);
        VectorStore vectorStore = mock(VectorStore.class);

        ChatClient.Builder builder = ChatClient.builder(chatModel);
        RagService service = new RagService(builder, vectorStore);

        service.ingest(List.of("passage one", "passage two"));

        verify(vectorStore).add(argThat(docs -> docs.size() == 2));
    }
}
