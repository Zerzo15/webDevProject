package com.example.webApplication.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Exception.IllegalOrderTransitionException;

import static com.example.webApplication.Entity.OrderStatus.AWAITING_PICKUP;
import static com.example.webApplication.Entity.OrderStatus.CANCELLED;
import static com.example.webApplication.Entity.OrderStatus.COMPLETED;
import static com.example.webApplication.Entity.OrderStatus.DELIVERED;
import static com.example.webApplication.Entity.OrderStatus.PENDING_CONFIRMATION;
import static com.example.webApplication.Entity.OrderStatus.SHIPPING;
import static com.example.webApplication.Service.OrderActor.ADMIN;
import static com.example.webApplication.Service.OrderActor.CUSTOMER;
import static com.example.webApplication.Service.OrderActor.STAFF;

/**
 * The only place allowed to change Order.status (proposal 6.1).
 * Controllers and services must call transition(...) instead of order.setStatus(...).
 *
 * The caller owns the transaction and saves the order.
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Map<OrderStatus, Set<OrderActor>>> RULES =
            new EnumMap<>(OrderStatus.class);

    static {
        allow(PENDING_CONFIRMATION, AWAITING_PICKUP, STAFF, ADMIN);
        // The customer may cancel freely only while the order waits for confirmation.
        allow(PENDING_CONFIRMATION, CANCELLED, CUSTOMER, STAFF, ADMIN);
        allow(AWAITING_PICKUP, SHIPPING, STAFF, ADMIN);
        // From AWAITING_PICKUP the customer cannot cancel; staff/admin can, before the goods leave the shop.
        allow(AWAITING_PICKUP, CANCELLED, STAFF, ADMIN);
        // From SHIPPING onward the order cannot be cancelled.
        allow(SHIPPING, DELIVERED, STAFF, ADMIN);
        // The customer confirms receipt to complete the order.
        allow(DELIVERED, COMPLETED, CUSTOMER);
        // COMPLETED and CANCELLED are final: no rules registered.
    }

    private static void allow(OrderStatus from, OrderStatus to, OrderActor first, OrderActor... rest) {
        RULES.computeIfAbsent(from, k -> new EnumMap<>(OrderStatus.class))
                .put(to, EnumSet.of(first, rest));
    }

    public boolean canTransition(OrderStatus from, OrderStatus to, OrderActor actor) {
        if (from == null || to == null || actor == null) {
            return false;
        }
        Map<OrderStatus, Set<OrderActor>> targets = RULES.get(from);
        if (targets == null) {
            return false;
        }
        Set<OrderActor> actors = targets.get(to);
        return actors != null && actors.contains(actor);
    }

    public Set<OrderStatus> allowedTargets(OrderStatus from, OrderActor actor) {
        Set<OrderStatus> result = EnumSet.noneOf(OrderStatus.class);
        for (OrderStatus candidate : OrderStatus.values()) {
            if (canTransition(from, candidate, actor)) {
                result.add(candidate);
            }
        }
        return result;
    }

    public boolean isFinal(OrderStatus status) {
        return status == COMPLETED || status == CANCELLED;
    }

    /**
     * Moves the order to the target status or throws IllegalOrderTransitionException.
     * Does not save the order.
     */
    public Order transition(Order order, OrderStatus target, OrderActor actor) {
        if (order == null) {
            throw new IllegalArgumentException("order is required");
        }
        OrderStatus current = order.getStatus();
        if (!canTransition(current, target, actor)) {
            throw new IllegalOrderTransitionException(current, target, actor);
        }
        order.setStatus(target);
        return order;
    }
}
