package com.weather.pincode.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenWeatherClient {

    private final RestTemplate restTemplate;

    // These should be class fields, not local variables
    private final String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather";
    private final String forecastApiUrl = "https://api.openweathermap.org/data/2.5/forecast"; // <-- MOVED HERE

    @Value("${openweathermap.api.key}")
    private String apiKey;

    public OpenWeatherClient() {
        this.restTemplate = new RestTemplate();
    }

    public String getWeather(double latitude, double longitude) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s", weatherApiUrl, latitude, longitude, apiKey);
        return restTemplate.getForObject(url, String.class);
    }
    
    public String getForecast(double latitude, double longitude) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s", forecastApiUrl, latitude, longitude, apiKey);
        return restTemplate.getForObject(url, String.class);
    }
}