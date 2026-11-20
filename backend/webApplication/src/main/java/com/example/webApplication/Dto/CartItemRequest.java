package com.example.webApplication.Dto;

/**
 * Body for adding a variant to the cart (variantId + quantity) or changing the
 * quantity of a line (only quantity is used, variantId may be null).
 */
public record CartItemRequest(Long variantId, Integer quantity) {
}
