package com.example.webApplication.Exception;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponse> handleOutOfStockException(OutOfStockException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "Out of stock", ex.getMessage());
    }

    @ExceptionHandler(InvalidVoucherException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVoucherException(InvalidVoucherException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid voucher: " + ex.getReason(), ex.getMessage());
    }

    @ExceptionHandler(IllegalOrderTransitionException.class)
    public ResponseEntity<ErrorResponse> handleIllegalOrderTransitionException(IllegalOrderTransitionException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "Illegal order transition", ex.getMessage());
    }

    // Without this handler a 403 raised by @PreAuthorize would be turned into a 500 by the catch-all below.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access denied", "You do not have permission to do this");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", "Login ID or password is incorrect");
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
