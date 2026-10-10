package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.ReturnOrderStatus;

public record ReturnOrderDto(
    Long id,
    OrderDto order,
    UserDto appUser,
    String reason,
    ReturnOrderStatus status,
    BigDecimal refund_amount,
    Instant created_at,
    Instant updated_at
) {

}
