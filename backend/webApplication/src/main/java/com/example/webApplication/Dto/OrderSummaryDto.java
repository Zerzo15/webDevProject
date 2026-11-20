package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;

/**
 * A row of an order list.
 */
public record OrderSummaryDto(
        long id,
        OrderStatus status,
        String paymentMethod,
        BigDecimal totalAmount,
        String trackingCode,
        Instant createdAt) {

    public static OrderSummaryDto from(Order o) {
        return new OrderSummaryDto(o.getId(), o.getStatus(), o.getPaymentMethod(), o.getTotalAmount(),
                o.getTrackingCode(), o.getCreatedAt());
    }
}
