package com.sangiya.springai.function;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class FunctionCallingController {

    private final ChatClient.Builder builder;

    @PostMapping("/weather")
    public WeatherQueryResponse askWeather(@RequestBody WeatherQueryRequest request) {
        String answer = builder.build()
                .prompt()
                .system("You are a helpful assistant. Use the currentWeather tool when asked about weather.")
                .user(request.question())
                .functions("currentWeather")
                .call()
                .content();
        return new WeatherQueryResponse(answer);
    }

    record WeatherQueryRequest(String question) {}
    record WeatherQueryResponse(String answer) {}
}
