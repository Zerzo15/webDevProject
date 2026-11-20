package com.example.webApplication.Dto;

import java.time.Instant;

import com.example.webApplication.Entity.OrderStatus;

/**
 * One entry of the status history: who changed the order and when. fromStatus is null for the first entry.
 */
public record OrderHistoryDto(
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String changedBy,
        String reason,
        Instant changedAt) {
}
