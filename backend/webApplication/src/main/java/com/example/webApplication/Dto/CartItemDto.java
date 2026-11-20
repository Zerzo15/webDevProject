package com.example.webApplication.Dto;

import java.math.BigDecimal;

/**
 * One line of the cart with the current price and stock.
 * inStock is false when the quantity in the cart is more than what the shop has left.
 */
public record CartItemDto(
        long id,
        long variantId,
        long productId,
        String productName,
        String size,
        String color,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal,
        int availableStock,
        boolean inStock) {
}
