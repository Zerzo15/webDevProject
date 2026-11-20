package com.example.webApplication.Dto;

/**
 * Optional body when cancelling an order; the reason is stored in the status history.
 */
public record CancelOrderRequest(String reason) {
}
