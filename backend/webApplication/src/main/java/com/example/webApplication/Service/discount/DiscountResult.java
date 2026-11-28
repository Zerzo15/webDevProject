package com.example.webApplication.Service.discount;

import java.math.BigDecimal;

/** Hai khoan giam, khop voi orders.discount_amount va orders.shipping_discount. */
public record DiscountResult(BigDecimal discountAmount, BigDecimal shippingDiscount) {

    public static DiscountResult onGoods(BigDecimal amount) {
        return new DiscountResult(amount, BigDecimal.ZERO);
    }

    public static DiscountResult onShipping(BigDecimal amount) {
        return new DiscountResult(BigDecimal.ZERO, amount);
    }
}
