package com.example.webApplication.Exception;

/**
 * Thrown when the data sent by the client is not acceptable (for example a quantity below 1).
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
