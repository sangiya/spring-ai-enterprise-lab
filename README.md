# Spring AI Enterprise Lab

Enterprise patterns for [Spring AI](https://docs.spring.io/spring-ai/reference/) covering the core production use-cases: chat with conversation memory, RAG (Retrieval-Augmented Generation), and function calling (tool use). All patterns compile to a single runnable Spring Boot 3 application.

## Features

| Pattern | Endpoint | Description |
|---|---|---|
| Chat + Memory | `POST /api/chat` | Stateful multi-turn conversation; history stored per `conversationId` |
| Streaming | `POST /api/chat/stream` | Token-by-token SSE stream for real-time UI updates |
| RAG | `POST /api/rag/ingest`, `POST /api/rag/query` | Ingest text → embeddings → vector store; query with context injection |
| Function Calling | `POST /api/tools/weather` | Model invokes `currentWeather` function when asked about weather |

## Architecture

```
ChatController ──► ChatService ──► ChatClient (Spring AI)
                                        │
                                   MessageChatMemoryAdvisor (per-conv history)
                                        │
                                   OpenAI Chat Model

RagController ──► RagService ──► ChatClient
                                       │
                                  QuestionAnswerAdvisor
                                       │
                              SimpleVectorStore ◄── EmbeddingModel
                              (cosine similarity)

FunctionCallingController ──► ChatClient.functions("currentWeather")
                                       │
                              WeatherService::currentWeather (bean)
```

## Running locally

You need an OpenAI API key:

```bash
export OPENAI_API_KEY=sk-...
mvn spring-boot:run
```

```bash
# Multi-turn chat
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"session-1","message":"What is dependency injection?"}'

# RAG: ingest then query
curl -X POST http://localhost:8080/api/rag/ingest \
  -H "Content-Type: application/json" \
  -d '{"passages":["Spring Boot 3 requires Java 17 or higher.","Spring AI 1.0 supports OpenAI, Anthropic, and Ollama."]}'

curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question":"What Java version does Spring Boot 3 require?"}'

# Function calling
curl -X POST http://localhost:8080/api/tools/weather \
  -H "Content-Type: application/json" \
  -d '{"question":"What is the weather in London?"}'
```

## Running tests (no API key needed)

```bash
mvn test
```

Tests mock `ChatModel` and `EmbeddingModel` directly — no HTTP call is ever made to OpenAI during CI.

## Swapping providers

Spring AI's abstraction means switching from OpenAI to Anthropic is a one-dependency change:

```xml
<!-- Remove spring-ai-openai-spring-boot-starter -->
<!-- Add -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
</dependency>
```

And update `application.yml`:
```yaml
spring.ai.anthropic.api-key: ${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.options.model: claude-sonnet-4-6
```

## License

MIT
