package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.OrderStatus;

/**
 * The five filters of the order list for staff and admin (proposal 5.2). Every field is optional.
 * keyword matches the order number or part of the customer email.
 */
public record OrderAdminFilter(
        OrderStatus status,
        Instant createdFrom,
        Instant createdTo,
        BigDecimal totalFrom,
        BigDecimal totalTo,
        String paymentMethod,
        String keyword) {
}
