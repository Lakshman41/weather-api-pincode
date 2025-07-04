package com.weather.pincode.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
public class ExternalApiFailureException extends RuntimeException {
    public ExternalApiFailureException(String message) {
        super(message);
    }
}