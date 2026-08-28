package com.sangiya.springai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * RAG (Retrieval-Augmented Generation) service.
 *
 * Documents are ingested into an in-memory vector store. On each query,
 * QuestionAnswerAdvisor retrieves the top-k most semantically similar
 * documents and prepends them as context to the prompt so the model can
 * answer questions grounded in the ingested content rather than hallucinating.
 */
@Service
@Slf4j
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = builder
                .defaultSystem("""
                        You are a precise assistant. Answer questions using ONLY the provided context.
                        If the context does not contain enough information, say so explicitly.
                        """)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.builder().topK(4).build()))
                .build();
    }

    /** Ingest a list of text passages into the vector store. */
    public void ingest(List<String> passages) {
        List<Document> docs = passages.stream()
                .map(text -> new Document(text, Map.of("source", "api-ingestion")))
                .toList();
        vectorStore.add(docs);
        log.info("Ingested {} documents into vector store", docs.size());
    }

    /** Query the vector store + LLM with a user question. */
    public String query(String question) {
        log.info("RAG query: {}", question);
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
