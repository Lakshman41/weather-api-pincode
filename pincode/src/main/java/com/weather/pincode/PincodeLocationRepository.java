package com.weather.pincode.repository;

import com.weather.pincode.PincodeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PincodeLocationRepository extends JpaRepository<PincodeLocation, String> {
    // JpaRepository provides save(), findById(), etc. automatically.
}