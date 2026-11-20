package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.OrderStatus;

/**
 * A row of the order list for staff and admin.
 */
public record OrderAdminRowDto(
        long id,
        OrderStatus status,
        String customerEmail,
        String paymentMethod,
        BigDecimal totalAmount,
        String trackingCode,
        Instant createdAt) {
}
