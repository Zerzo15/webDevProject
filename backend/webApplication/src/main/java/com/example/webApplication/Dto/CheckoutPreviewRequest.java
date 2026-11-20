package com.example.webApplication.Dto;

/**
 * Body for previewing the total of the cart with an optional voucher code.
 */
public record CheckoutPreviewRequest(String voucherCode) {
}
