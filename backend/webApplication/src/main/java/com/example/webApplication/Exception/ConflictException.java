package com.example.webApplication.Exception;

/**
 * Thrown when the request clashes with existing data (for example a duplicate voucher code).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
