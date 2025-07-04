package com.weather.pincode.controller;

import com.weather.pincode.exception.InvalidInputException;
import com.weather.pincode.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api")
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // This is now the ONE AND ONLY endpoint, as per the assignment.
    @GetMapping("/weather")
    public ResponseEntity<String> getWeather(
            @RequestParam String pincode,
            @RequestParam("date") String dateString) {
        
        LocalDate date;
        try {
            // Converts the "YYYY-MM-DD" string from the request into a LocalDate object
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            // If the format is wrong, throw our custom exception
            throw new InvalidInputException("Invalid date format. Please use YYYY-MM-DD.");
        }

        // Call the single, unified service method
        String weatherData = weatherService.getWeatherForPincode(pincode, date);
        
        return ResponseEntity.ok().header("Content-Type", "application/json").body(weatherData);
    }
}