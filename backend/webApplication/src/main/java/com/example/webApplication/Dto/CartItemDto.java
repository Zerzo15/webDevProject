package com.example.webApplication.Dto;

import java.time.Instant;

public record CartItemDto(
    Long id,
    CartDto cart,
    ProductDto product,
    Long quantity,
    Instant added_at
) {

}
