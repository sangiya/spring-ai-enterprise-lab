package com.sangiya.springai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * In-memory vector store with cosine similarity search.
 *
 * Stores (text, embedding) pairs. On query, embeds the query text and returns
 * the top-k documents ranked by cosine similarity. This is the "retrieval"
 * step of Retrieval-Augmented Generation — in production you would replace
 * this with pgvector, Redis, or a dedicated vector database.
 */
@Component
@Slf4j
public class VectorStore {

    private final List<IndexedDocument> index = new CopyOnWriteArrayList<>();
    private Function<String, float[]> embedder;

    public void setEmbedder(Function<String, float[]> embedder) {
        this.embedder = embedder;
    }

    public void add(List<String> passages) {
        for (String text : passages) {
            float[] vector = embedder.apply(text);
            index.add(new IndexedDocument(text, vector));
            log.debug("Indexed passage ({} chars)", text.length());
        }
        log.info("VectorStore now contains {} documents", index.size());
    }

    public List<String> search(String query, int topK) {
        float[] queryVector = embedder.apply(query);
        return index.stream()
                .sorted(Comparator.comparingDouble(
                        (IndexedDocument doc) -> cosineSimilarity(queryVector, doc.embedding()))
                        .reversed())
                .limit(topK)
                .map(IndexedDocument::text)
                .toList();
    }

    public int size() {
        return index.size();
    }

    private static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0 : dot / denom;
    }

    record IndexedDocument(String text, float[] embedding) {}
}
