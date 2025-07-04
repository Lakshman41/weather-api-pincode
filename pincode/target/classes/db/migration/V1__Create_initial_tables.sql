-- V1__Create_initial_tables.sql

-- This table caches the mapping from pincode to lat/lon
CREATE TABLE pincode_location (
    pincode VARCHAR(10) PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- This table caches the current weather data for a specific day
CREATE TABLE weather_cache (
    id BIGSERIAL PRIMARY KEY,
    pincode VARCHAR(10) NOT NULL,
    for_date DATE NOT NULL,
    weather_data TEXT,
    cached_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- This table caches the 5-day forecast data
CREATE TABLE forecast_cache (
    id BIGSERIAL PRIMARY KEY,
    pincode VARCHAR(10) NOT NULL UNIQUE,
    forecast_data TEXT,
    cached_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Add indexes for faster lookups
CREATE INDEX idx_weather_cache_pincode_date ON weather_cache (pincode, for_date);