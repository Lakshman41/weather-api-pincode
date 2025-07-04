package com.weather.pincode;

import com.fasterxml.jackson.annotation.JsonProperty; // <-- ADD THIS IMPORT
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class PincodeLocation {

    @Id
    @JsonProperty("zip") // Map the JSON field "zip" to this Java field
    private String pincode;

    @JsonProperty("lat") // Map the JSON field "lat" to this Java field
    private Double latitude;

    @JsonProperty("lon") // Map the JSON field "lon" to this Java field
    private Double longitude;
    
    // These fields are for our own tracking and are not from the JSON response
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    // Getters and Setters remain the same
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}