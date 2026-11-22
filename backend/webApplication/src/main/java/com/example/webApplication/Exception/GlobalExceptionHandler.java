package com.example.webApplication.Exception;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRegistrationException(InvalidRegistrationException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid registration", ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "User already exists", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", "Login ID or password is incorrect");
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenException(Exception ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Refresh token expired", "Try login again");
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String error, String message) {
        ErrorResponse errorResponse = new ErrorResponse(
                Timestamp.valueOf(LocalDateTime.now()),
                String.valueOf(status.value()),
                error,
                message);

        return new ResponseEntity<>(errorResponse, status);
    }

}
