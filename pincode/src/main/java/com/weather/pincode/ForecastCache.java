package com.weather.pincode;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ForecastCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // Each pincode can only have one forecast cache entry
    private String pincode;

    @Column(columnDefinition = "TEXT")
    private String forecastData;

    private Instant cachedAt = Instant.now();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getForecastData() { return forecastData; }
    public void setForecastData(String forecastData) { this.forecastData = forecastData; }
    public Instant getCachedAt() { return cachedAt; }
    public void setCachedAt(Instant cachedAt) { this.cachedAt = cachedAt; }
}