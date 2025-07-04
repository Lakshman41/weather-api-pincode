package com.weather.pincode.client;

import com.weather.pincode.PincodeLocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeocodingClient {

    private final RestTemplate restTemplate;
    private final String geocodingApiUrl = "http://api.openweathermap.org/geo/1.0/zip";

    @Value("${openweathermap.api.key}")
    private String apiKey;

    public GeocodingClient() {
        this.restTemplate = new RestTemplate();
    }

    public PincodeLocation getCoordinatesForPincode(String pincode) {
        // For this API, we assume the country code is for India (IN)
        String url = String.format("%s?zip=%s,IN&appid=%s", geocodingApiUrl, pincode, apiKey);

        // The geocoding API returns a JSON object that we can map directly to a PincodeLocation object.
        // Spring's RestTemplate will handle the JSON-to-Object conversion automatically.
        return restTemplate.getForObject(url, PincodeLocation.class);
    }
}