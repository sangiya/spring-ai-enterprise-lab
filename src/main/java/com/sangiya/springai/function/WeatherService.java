package com.sangiya.springai.function;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * Function-calling (tool use) example.
 *
 * Spring AI discovers @Bean methods that return Function and makes them
 * available as tools the model can call. When the model decides it needs
 * live weather data it emits a function-call request; Spring AI invokes
 * the bean and returns the result to the model as a tool message.
 */
@Configuration
@Slf4j
public class WeatherService {

    public record WeatherRequest(String city, String unit) {}
    public record WeatherResponse(String city, double temperature, String unit, String condition) {}

    @Bean
    @Description("Get the current weather for a given city")
    public Function<WeatherRequest, WeatherResponse> currentWeather() {
        return request -> {
            log.info("Weather function called for city={} unit={}", request.city(), request.unit());
            // Stub — returns synthetic data; a real implementation would call an external API
            double temperature = "metric".equalsIgnoreCase(request.unit()) ? 22.5 : 72.5;
            return new WeatherResponse(request.city(), temperature, request.unit(), "Partly cloudy");
        };
    }
}
