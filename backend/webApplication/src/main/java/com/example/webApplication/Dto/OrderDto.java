package com.example.webApplication.Dto;

import com.example.webApplication.Entity.OrderStatus;

public record OrderDto(
    UserDto user,
    OrderStatus status,
    String payment_method
) {

}
