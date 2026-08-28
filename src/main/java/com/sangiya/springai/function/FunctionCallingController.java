package com.sangiya.springai.function;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class FunctionCallingController {

    private final WeatherService weatherService;

    @PostMapping("/weather")
    public WeatherQueryResponse askWeather(@RequestBody WeatherQueryRequest request) {
        return new WeatherQueryResponse(weatherService.askAboutWeather(request.question()));
    }

    record WeatherQueryRequest(String question) {}
    record WeatherQueryResponse(String answer) {}
}
