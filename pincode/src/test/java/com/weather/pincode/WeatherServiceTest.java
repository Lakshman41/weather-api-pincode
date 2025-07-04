package com.weather.pincode;

import com.weather.pincode.client.GeocodingClient;
import com.weather.pincode.client.OpenWeatherClient;
import com.weather.pincode.exception.InvalidInputException;
import com.weather.pincode.exception.ResourceNotFoundException;
import com.weather.pincode.repository.ForecastCacheRepository;
import com.weather.pincode.repository.PincodeLocationRepository;
import com.weather.pincode.repository.WeatherCacheRepository;
import com.weather.pincode.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Use Mockito's extension for JUnit 5 to automatically handle mock creation.
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    // @Mock creates a fake version of this class. We can tell it how to behave.
    @Mock
    private WeatherCacheRepository weatherCacheRepository;
    @Mock
    private PincodeLocationRepository pincodeLocationRepository;
    @Mock
    private ForecastCacheRepository forecastCacheRepository;
    @Mock
    private GeocodingClient geocodingClient;
    @Mock
    private OpenWeatherClient openWeatherClient;

    // @InjectMocks creates an instance of WeatherService and automatically injects
    // all the @Mock objects defined above into it.
    @InjectMocks
    private WeatherService weatherService;

    private PincodeLocation sampleLocation;
    private final String PINCODE = "411014";
    private final String FAKE_WEATHER_JSON = "{\"weather\":\"sunny\"}";
    private final String FAKE_FORECAST_JSON = "{\"list\":[{\"dt_txt\":\"2025-07-06 00:00:00\"}]}";


    @BeforeEach
    void setUp() {
        // Create a sample location object to use in multiple tests
        sampleLocation = new PincodeLocation();
        sampleLocation.setPincode(PINCODE);
        sampleLocation.setLatitude(18.52);
        sampleLocation.setLongitude(73.85);
    }

    @Test
    @DisplayName("Should return cached CURRENT weather data if found and fresh")
    void testGetCurrentWeather_cacheHit() {
        // --- ARRANGE ---
        // Prepare a fake cached weather object
        WeatherCache cachedWeather = new WeatherCache();
        cachedWeather.setWeatherData(FAKE_WEATHER_JSON);

        // Tell our mock repository: "When findByPincodeAndForDate is called with these exact arguments, return our fake object."
        when(weatherCacheRepository.findByPincodeAndForDate(PINCODE, LocalDate.now())).thenReturn(Optional.of(cachedWeather));

        // --- ACT ---
        String result = weatherService.getWeatherForPincode(PINCODE, LocalDate.now());

        // --- ASSERT ---
        assertEquals(FAKE_WEATHER_JSON, result);
        // Verify that the external OpenWeatherClient was NEVER called, proving we used the cache.
        verify(openWeatherClient, never()).getWeather(anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should fetch new CURRENT weather data if not in cache")
    void testGetCurrentWeather_cacheMiss() {
        // --- ARRANGE ---
        // Cache is empty for both weather and pincode location
        when(weatherCacheRepository.findByPincodeAndForDate(anyString(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(pincodeLocationRepository.findById(PINCODE)).thenReturn(Optional.empty());
        
        // When the geocoding client is called, return our sample location
        when(geocodingClient.getCoordinatesForPincode(PINCODE)).thenReturn(sampleLocation);

        // Tell the mock repository: When save is called with any PincodeLocation, just return that same location.
        when(pincodeLocationRepository.save(any(PincodeLocation.class))).thenReturn(sampleLocation);
        
        // When the weather client is called, return our fake weather JSON
        when(openWeatherClient.getWeather(sampleLocation.getLatitude(), sampleLocation.getLongitude())).thenReturn(FAKE_WEATHER_JSON);

        // --- ACT ---
        String result = weatherService.getWeatherForPincode(PINCODE, LocalDate.now());

        // --- ASSERT ---
        assertEquals(FAKE_WEATHER_JSON, result);
        // Verify that the external weather client WAS called exactly once.
        verify(openWeatherClient, times(1)).getWeather(18.52, 73.85);
        // Verify that we saved the new weather data to the cache.
        verify(weatherCacheRepository, times(1)).save(any(WeatherCache.class));
    }

    @Test
    @DisplayName("Should return filtered FORECAST for a future date")
    void testGetFutureForecast_cacheMiss() {
        LocalDate futureDate = LocalDate.of(2025, 7, 6);
        
        // --- ARRANGE ---
        // Caches are empty
        when(forecastCacheRepository.findByPincode(PINCODE)).thenReturn(Optional.empty());
        when(pincodeLocationRepository.findById(PINCODE)).thenReturn(Optional.of(sampleLocation));
        
        // When the forecast client is called, return the fake forecast JSON
        when(openWeatherClient.getForecast(anyDouble(), anyDouble())).thenReturn(FAKE_FORECAST_JSON);

        // --- ACT ---
        String result = weatherService.getWeatherForPincode(PINCODE, futureDate);

        // --- ASSERT ---
        assertNotNull(result);
        assertTrue(result.contains("dailyForecasts")); // Check that the response is in our filtered format
        // Verify we saved the full forecast to the cache
        verify(forecastCacheRepository, times(1)).save(any(ForecastCache.class));
    }

    @Test
    @DisplayName("Should throw InvalidInputException for a past date")
    void testPastDate_throwsException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // --- ACT & ASSERT ---
        // Assert that calling the method with a past date throws the correct exception
        assertThrows(InvalidInputException.class, () -> {
            weatherService.getWeatherForPincode(PINCODE, pastDate);
        });
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for an invalid pincode")
    void testInvalidPincode_throwsException() {
        // --- ARRANGE ---
        String invalidPincode = "999999";
        // Tell the geocoding client to simulate a failure by throwing an exception
        when(geocodingClient.getCoordinatesForPincode(invalidPincode)).thenThrow(new RestClientException("404 Not Found"));
        
        // --- ACT & ASSERT ---
        assertThrows(ResourceNotFoundException.class, () -> {
            weatherService.getWeatherForPincode(invalidPincode, LocalDate.now());
        });
    }
}