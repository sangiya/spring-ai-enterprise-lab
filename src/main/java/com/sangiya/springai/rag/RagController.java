package com.sangiya.springai.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(@RequestBody IngestRequest request) {
        ragService.ingest(request.passages());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/query")
    public QueryResponse query(@RequestBody QueryRequest request) {
        return new QueryResponse(ragService.query(request.question()));
    }

    record IngestRequest(List<String> passages) {}
    record QueryRequest(String question) {}
    record QueryResponse(String answer) {}
}
