package com.example.webApplication.Dto;

public record OrderItemDto(
    Long id,
    OrderDto order,
    ProductDto product,
    Integer quantity,
    Long unit_price,
    Long subtotal
) {

}
