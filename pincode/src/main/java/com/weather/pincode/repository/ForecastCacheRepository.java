package com.weather.pincode.repository;

import com.weather.pincode.ForecastCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForecastCacheRepository extends JpaRepository<ForecastCache, Long> {

    // Custom query to find a forecast cache by its pincode
    Optional<ForecastCache> findByPincode(String pincode);
}