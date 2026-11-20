package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.webApplication.Entity.OrderStatus;

/**
 * An order with its lines, money details and status history.
 * subtotal is the sum of the lines; total = subtotal - discountAmount + shippingFee - shippingDiscount.
 */
public record OrderDetailDto(
        long id,
        OrderStatus status,
        String paymentMethod,
        String shippingAddress,
        String customerEmail,
        String voucherCode,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscount,
        BigDecimal totalAmount,
        String trackingCode,
        Instant createdAt,
        List<OrderLineDto> lines,
        List<OrderHistoryDto> history) {
}
