package com.example.webApplication.Dto;

public record ProductVariantDto(
    Long id,
    Long productId,
    String size,
    String color,
    int stockQuantity
) {
}
