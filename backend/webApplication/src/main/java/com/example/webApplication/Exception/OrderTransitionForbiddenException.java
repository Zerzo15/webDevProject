package com.example.webApplication.Exception;

import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.UserRoleStatus;

public class OrderTransitionForbiddenException extends RuntimeException {
    public OrderTransitionForbiddenException(OrderStatus from, OrderStatus to, UserRoleStatus role) {
        super("Role " + role + " cannot change order status from " + from + " to " + to);
    }
}
