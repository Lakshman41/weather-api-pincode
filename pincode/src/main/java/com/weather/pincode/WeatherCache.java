package com.weather.pincode;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class WeatherCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private LocalDate forDate;

    @Column(columnDefinition = "TEXT") // Use TEXT for flexibility with JSON
    private String weatherData;
    
    private Instant cachedAt = Instant.now();

    // Getters and Setters
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public LocalDate getForDate() { return forDate; }
    public void setForDate(LocalDate forDate) { this.forDate = forDate; }
    public String getWeatherData() { return weatherData; }
    public void setWeatherData(String weatherData) { this.weatherData = weatherData; }
    // ... (you can add getters/setters for id and cachedAt if needed)
}