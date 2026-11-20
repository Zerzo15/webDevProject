package com.example.webApplication.Exception;

import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Service.OrderActor;

/**
 * Thrown when an order status change is not allowed for the given actor.
 */
public class IllegalOrderTransitionException extends RuntimeException {

    private final OrderStatus from;
    private final OrderStatus to;
    private final OrderActor actor;

    public IllegalOrderTransitionException(OrderStatus from, OrderStatus to, OrderActor actor) {
        super("Order cannot move from " + from + " to " + to + " by " + actor);
        this.from = from;
        this.to = to;
        this.actor = actor;
    }

    public OrderStatus getFrom() {
        return from;
    }

    public OrderStatus getTo() {
        return to;
    }

    public OrderActor getActor() {
        return actor;
    }
}
