package com.example.webApplication.Dto;

/**
 * Body for placing an order from the cart. voucherCode is optional.
 */
public record PlaceOrderRequest(String shippingAddress, String voucherCode) {
}
