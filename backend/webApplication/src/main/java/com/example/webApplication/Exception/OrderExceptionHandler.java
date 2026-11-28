package com.example.webApplication.Exception;

import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Uu tien cao nhat de khong bi handler Exception chung bat truoc. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OrderExceptionHandler {

    @ExceptionHandler(InvalidOrderTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidOrderTransitionException ex) {
        return error(HttpStatus.CONFLICT, "Invalid order transition", ex.getMessage());
    }

    @ExceptionHandler(OrderTransitionForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(OrderTransitionForbiddenException ex) {
        return error(HttpStatus.FORBIDDEN, "Order transition not allowed", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(
            Timestamp.from(Instant.now()), String.valueOf(status.value()), error, message));
    }
}
