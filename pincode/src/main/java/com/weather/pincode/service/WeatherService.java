package com.weather.pincode.service;

// All necessary imports
import com.weather.pincode.ForecastCache;
import com.weather.pincode.PincodeLocation;
import com.weather.pincode.WeatherCache;
import com.weather.pincode.client.GeocodingClient;
import com.weather.pincode.client.OpenWeatherClient;
import com.weather.pincode.exception.ExternalApiFailureException;
import com.weather.pincode.exception.InvalidInputException;
import com.weather.pincode.exception.ResourceNotFoundException;
import com.weather.pincode.repository.ForecastCacheRepository;
import com.weather.pincode.repository.PincodeLocationRepository;
import com.weather.pincode.repository.WeatherCacheRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherService {

    private final WeatherCacheRepository weatherCacheRepository;
    private final PincodeLocationRepository pincodeLocationRepository;
    private final ForecastCacheRepository forecastCacheRepository;
    private final GeocodingClient geocodingClient;
    private final OpenWeatherClient openWeatherClient;

    @Autowired
    public WeatherService(WeatherCacheRepository weatherCacheRepository, PincodeLocationRepository pincodeLocationRepository, ForecastCacheRepository forecastCacheRepository, GeocodingClient geocodingClient, OpenWeatherClient openWeatherClient) {
        this.weatherCacheRepository = weatherCacheRepository;
        this.pincodeLocationRepository = pincodeLocationRepository;
        this.forecastCacheRepository = forecastCacheRepository;
        this.geocodingClient = geocodingClient;
        this.openWeatherClient = openWeatherClient;
    }

    @Transactional
    public String getWeatherForPincode(String pincode, LocalDate date) {
        // Step 1: Validate inputs
        if (!pincode.matches("\\d{6}")) {
            throw new InvalidInputException("Pincode must be exactly 6 digits.");
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new InvalidInputException("Invalid date. Historical weather data is not supported.");
        }
        if (date.isAfter(today.plusDays(5))) {
            throw new InvalidInputException("Invalid date. Forecast is only available for the next 5 days.");
        }

        // Step 2: Route logic based on the date
        if (date.equals(today)) {
            return getCurrentWeather(pincode, date);
        } else {
            return getFutureDailyForecast(pincode, date);
        }
    }
    
    private String getCurrentWeather(String pincode, LocalDate date) {
        Optional<WeatherCache> cachedWeather = weatherCacheRepository.findByPincodeAndForDate(pincode, date);
        if (cachedWeather.isPresent()) {
            System.out.println("Cache HIT for weather data: " + pincode + " on " + date);
            return cachedWeather.get().getWeatherData();
        }

        System.out.println("Cache MISS for weather data. Fetching new data.");
        PincodeLocation location = getPincodeLocation(pincode);
        String newWeatherData = openWeatherClient.getWeather(location.getLatitude(), location.getLongitude());
        
        WeatherCache newCacheEntry = new WeatherCache();
        newCacheEntry.setPincode(pincode);
        newCacheEntry.setForDate(date);
        newCacheEntry.setWeatherData(newWeatherData);
        // We can use saveAndFlush here, it's efficient for new entities.
        weatherCacheRepository.saveAndFlush(newCacheEntry);
        return newWeatherData;
    }

    private String getFutureDailyForecast(String pincode, LocalDate date) {
        String fullForecastJson = getFullForecast(pincode);

        // Parse and filter the JSON to get only the data for the requested date
        JSONObject forecastObject = new JSONObject(fullForecastJson);
        JSONArray forecastList = forecastObject.getJSONArray("list");
        JSONArray dailyEntries = new JSONArray();

        String dateString = date.toString(); // "YYYY-MM-DD"

        for (int i = 0; i < forecastList.length(); i++) {
            JSONObject entry = forecastList.getJSONObject(i);
            if (entry.getString("dt_txt").startsWith(dateString)) {
                dailyEntries.put(entry);
            }
        }

        if (dailyEntries.isEmpty()) {
            throw new ResourceNotFoundException("No forecast data found for date: " + dateString);
        }

        // Return a new JSON object containing only the filtered list
        return new JSONObject().put("dailyForecasts", dailyEntries).toString();
    }

    private String getFullForecast(String pincode) {
        Optional<ForecastCache> cachedForecastOpt = forecastCacheRepository.findByPincode(pincode);
        
        // Check if a fresh cache exists
        if (cachedForecastOpt.isPresent() && Duration.between(cachedForecastOpt.get().getCachedAt(), Instant.now()).toHours() < 3) {
            System.out.println("Cache HIT for forecast data: " + pincode);
            return cachedForecastOpt.get().getForecastData();
        }

        System.out.println("Cache MISS for forecast data. Fetching new forecast.");
        PincodeLocation location = getPincodeLocation(pincode);
        String newForecastData = openWeatherClient.getForecast(location.getLatitude(), location.getLongitude());

        // *** OPTIMIZED LOGIC ***
        // If a cache entry already exists (but was stale), update it. Otherwise, create a new one.
        ForecastCache cacheToSave = cachedForecastOpt.orElseGet(ForecastCache::new);
        if (cacheToSave.getId() == null) { // This is how we know it's a new entity
            cacheToSave.setPincode(pincode);
        }
        cacheToSave.setForecastData(newForecastData);
        cacheToSave.setCachedAt(Instant.now());
        forecastCacheRepository.saveAndFlush(cacheToSave); // saveAndFlush on a managed/new entity is efficient
        return newForecastData;
    }
    
    private PincodeLocation getPincodeLocation(String pincode) {
        Optional<PincodeLocation> locationOpt = pincodeLocationRepository.findById(pincode);
        if (locationOpt.isPresent()) {
            System.out.println("Cache HIT for pincode coordinates: " + pincode);
            return locationOpt.get();
        }

        System.out.println("Cache MISS for pincode coordinates. Fetching from Geocoding API.");
        try {
            PincodeLocation newLocation = geocodingClient.getCoordinatesForPincode(pincode);
            if (newLocation == null || newLocation.getLatitude() == null) {
                throw new ResourceNotFoundException("Could not find coordinates for pincode: " + pincode);
            }
            newLocation.setPincode(pincode);
            // Because this is a guaranteed new entity, saveAndFlush will perform a direct INSERT.
            return pincodeLocationRepository.saveAndFlush(newLocation);
        } catch (RestClientException e) {
            throw new ResourceNotFoundException("Pincode not found: " + pincode);
        }
    }
}