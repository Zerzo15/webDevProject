package com.example.webApplication.Dto;

import com.example.webApplication.Entity.ReturnOrderStatus;

public record ReturnOrderDto(
    Long id,
    Long orderItemId,
    String reason,
    ReturnOrderStatus status
) {
}
