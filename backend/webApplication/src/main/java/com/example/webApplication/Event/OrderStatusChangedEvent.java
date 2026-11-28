package com.example.webApplication.Event;

import com.example.webApplication.Entity.OrderStatus;

/**
 * Phat ra khi don doi trang thai. Ben lang nghe (email) nen dung
 * {@code @TransactionalEventListener} de chi chay sau khi giao dich commit.
 */
public record OrderStatusChangedEvent(
    long orderId,
    OrderStatus from,
    OrderStatus to,
    String changedBy
) {}
