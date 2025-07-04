package com.weather.pincode.repository;

import com.weather.pincode.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCache, Long> {
    
    // This is a custom query method. Spring Data JPA will automatically
    // implement it based on the method name. It's incredibly powerful.
    Optional<WeatherCache> findByPincodeAndForDate(String pincode, LocalDate forDate);
}