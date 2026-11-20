package com.example.webApplication.Exception;

public class InvalidRegistrationException extends RuntimeException {
    public InvalidRegistrationException(String message) {
        super(message);
    }
}
