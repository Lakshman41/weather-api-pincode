package com.weather.pincode.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, HttpClientErrorException.NotFound.class})
    public ResponseEntity<Object> handleNotFoundException(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Resource Not Found");
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Object> handleInvalidInputException(InvalidInputException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Invalid Input");
    }
    
    @ExceptionHandler(ExternalApiFailureException.class)
    public ResponseEntity<Object> handleApiFailureException(ExternalApiFailureException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_GATEWAY, "External Service Error");
    }

    // A helper method to build the JSON error response
    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status, String error) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, status);
    }
}