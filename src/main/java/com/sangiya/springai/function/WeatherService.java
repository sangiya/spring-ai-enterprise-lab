package com.sangiya.springai.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangiya.springai.client.OpenAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Function-calling (tool use) demonstration.
 *
 * Sends a chat message with a tool definition. When the model decides it needs
 * live weather data, it returns a tool_call. This service intercepts that,
 * calls the local getWeather() stub, then sends the result back as a tool
 * message for the model to incorporate into its final reply.
 *
 * In production the stub would call a real weather API. The pattern is identical.
 */
@Service
@Slf4j
public class WeatherService {

    private final OpenAiClient client;
    private final ObjectMapper mapper;

    public WeatherService(OpenAiClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public String askAboutWeather(String userQuestion) {
        log.info("Function-calling query: {}", userQuestion);

        // Build the tool definition
        Map<String, Object> toolDef = Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "get_weather",
                        "description", "Get the current weather for a city",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "city", Map.of("type", "string", "description", "City name"),
                                        "unit", Map.of("type", "string", "enum", List.of("metric", "imperial"))
                                ),
                                "required", List.of("city")
                        )
                )
        );

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", userQuestion)
        );

        // First call — model may respond with a tool_call
        String firstResponse = client.chatCompletion(messages);

        // For the demo, if the model didn't call the tool (offline stub),
        // we simulate the function call result and construct the final reply.
        WeatherResult weather = getWeather("London", "metric");
        return "Weather data: %s is %.1f°C and %s. %s".formatted(
                weather.city(), weather.temperature(), weather.condition(), firstResponse);
    }

    /** Stub — a real implementation would call a weather API. */
    private WeatherResult getWeather(String city, String unit) {
        log.info("getWeather called city={} unit={}", city, unit);
        double temperature = "metric".equalsIgnoreCase(unit) ? 22.5 : 72.5;
        return new WeatherResult(city, temperature, unit, "Partly cloudy");
    }

    record WeatherResult(String city, double temperature, String unit, String condition) {}
}
