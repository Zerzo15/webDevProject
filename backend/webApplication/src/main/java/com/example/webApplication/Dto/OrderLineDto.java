package com.example.webApplication.Dto;

import java.math.BigDecimal;

/**
 * One line of an order. unitPrice is the price fixed when the order was placed.
 */
public record OrderLineDto(
        long variantId,
        long productId,
        String productName,
        String size,
        String color,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {
}
