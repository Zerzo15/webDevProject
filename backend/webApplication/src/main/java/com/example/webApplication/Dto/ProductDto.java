package com.example.webApplication.Dto;

import java.math.BigDecimal;

public record ProductDto(
    Long id,
    Long categoryId,
    String name,
    String description,
    BigDecimal basePrice,
    String imageUrl
) {
}
