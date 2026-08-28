package com.sangiya.springai.rag;

import com.sangiya.springai.client.OpenAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RAG (Retrieval-Augmented Generation) service.
 *
 * 1. Ingest: store passages as (text, embedding) pairs in the VectorStore.
 * 2. Query: embed the question, retrieve the top-k most similar passages,
 *    prepend them as context in the prompt, then call the LLM. The model
 *    answers grounded in retrieved content rather than hallucinating.
 */
@Service
@Slf4j
public class RagService {

    private static final int TOP_K = 4;
    private final VectorStore vectorStore;
    private final OpenAiClient client;

    public RagService(VectorStore vectorStore, OpenAiClient client) {
        this.vectorStore = vectorStore;
        this.client = client;
        // Wire the embedder so VectorStore delegates to OpenAI Embeddings API
        vectorStore.setEmbedder(client::embed);
    }

    public void ingest(List<String> passages) {
        vectorStore.add(passages);
    }

    public String query(String question) {
        log.info("RAG query: {}", question);
        List<String> context = vectorStore.search(question, TOP_K);

        String contextBlock = context.isEmpty()
                ? "No relevant context found."
                : String.join("\n\n", context);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", """
                You are a precise assistant. Answer questions using ONLY the provided context.
                If the context does not contain enough information, say so explicitly.
                """));
        messages.add(Map.of("role", "user", "content",
                "Context:\n" + contextBlock + "\n\nQuestion: " + question));

        return client.chatCompletion(messages);
    }
}
