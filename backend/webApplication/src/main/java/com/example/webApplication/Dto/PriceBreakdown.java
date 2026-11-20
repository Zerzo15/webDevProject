package com.example.webApplication.Dto;

import java.math.BigDecimal;

/**
 * Money details of an order: total = subtotal - discount + shippingFee - shippingDiscount.
 */
public record PriceBreakdown(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscount,
        BigDecimal total) {
}
