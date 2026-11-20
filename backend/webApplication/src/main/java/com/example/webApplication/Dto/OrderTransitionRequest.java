package com.example.webApplication.Dto;

import com.example.webApplication.Entity.OrderStatus;

/**
 * Body for moving an order to another status. The reason is stored in the status history.
 */
public record OrderTransitionRequest(OrderStatus target, String reason) {
}
