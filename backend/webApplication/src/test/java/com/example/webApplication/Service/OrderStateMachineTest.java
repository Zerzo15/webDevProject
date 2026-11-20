package com.example.webApplication.Service;

import static com.example.webApplication.Entity.OrderStatus.AWAITING_PICKUP;
import static com.example.webApplication.Entity.OrderStatus.CANCELLED;
import static com.example.webApplication.Entity.OrderStatus.COMPLETED;
import static com.example.webApplication.Entity.OrderStatus.DELIVERED;
import static com.example.webApplication.Entity.OrderStatus.PENDING_CONFIRMATION;
import static com.example.webApplication.Entity.OrderStatus.SHIPPING;
import static com.example.webApplication.Service.OrderActor.ADMIN;
import static com.example.webApplication.Service.OrderActor.CUSTOMER;
import static com.example.webApplication.Service.OrderActor.STAFF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Exception.IllegalOrderTransitionException;

class OrderStateMachineTest {

    private final OrderStateMachine machine = new OrderStateMachine();

    private static Order orderIn(OrderStatus status) {
        Order order = new Order();
        order.setStatus(status);
        return order;
    }

    // ---- valid transitions ----

    @Test
    void staffAndAdminConfirmPendingOrder() {
        assertTrue(machine.canTransition(PENDING_CONFIRMATION, AWAITING_PICKUP, STAFF));
        assertTrue(machine.canTransition(PENDING_CONFIRMATION, AWAITING_PICKUP, ADMIN));
    }

    @Test
    void customerCannotConfirmPendingOrder() {
        assertFalse(machine.canTransition(PENDING_CONFIRMATION, AWAITING_PICKUP, CUSTOMER));
    }

    @Test
    void everyoneCanCancelPendingOrder() {
        assertTrue(machine.canTransition(PENDING_CONFIRMATION, CANCELLED, CUSTOMER));
        assertTrue(machine.canTransition(PENDING_CONFIRMATION, CANCELLED, STAFF));
        assertTrue(machine.canTransition(PENDING_CONFIRMATION, CANCELLED, ADMIN));
    }

    @Test
    void staffAndAdminShipAwaitingPickupOrder() {
        assertTrue(machine.canTransition(AWAITING_PICKUP, SHIPPING, STAFF));
        assertTrue(machine.canTransition(AWAITING_PICKUP, SHIPPING, ADMIN));
        assertFalse(machine.canTransition(AWAITING_PICKUP, SHIPPING, CUSTOMER));
    }

    @Test
    void staffAndAdminCancelBeforeShipping() {
        assertTrue(machine.canTransition(AWAITING_PICKUP, CANCELLED, STAFF));
        assertTrue(machine.canTransition(AWAITING_PICKUP, CANCELLED, ADMIN));
    }

    @Test
    void customerCannotCancelOnceAwaitingPickup() {
        assertFalse(machine.canTransition(AWAITING_PICKUP, CANCELLED, CUSTOMER));
    }

    @Test
    void staffAndAdminMarkShippingOrderDelivered() {
        assertTrue(machine.canTransition(SHIPPING, DELIVERED, STAFF));
        assertTrue(machine.canTransition(SHIPPING, DELIVERED, ADMIN));
        assertFalse(machine.canTransition(SHIPPING, DELIVERED, CUSTOMER));
    }

    @Test
    void shippingOrderCannotBeCancelled() {
        for (OrderActor actor : OrderActor.values()) {
            assertFalse(machine.canTransition(SHIPPING, CANCELLED, actor), "actor " + actor);
        }
    }

    @Test
    void onlyCustomerCompletesDeliveredOrder() {
        assertTrue(machine.canTransition(DELIVERED, COMPLETED, CUSTOMER));
        assertFalse(machine.canTransition(DELIVERED, COMPLETED, STAFF));
        assertFalse(machine.canTransition(DELIVERED, COMPLETED, ADMIN));
    }

    // ---- invalid transitions ----

    @Test
    void skippingStepsIsRejected() {
        assertFalse(machine.canTransition(PENDING_CONFIRMATION, SHIPPING, ADMIN));
        assertFalse(machine.canTransition(PENDING_CONFIRMATION, DELIVERED, ADMIN));
        assertFalse(machine.canTransition(PENDING_CONFIRMATION, COMPLETED, ADMIN));
        assertFalse(machine.canTransition(AWAITING_PICKUP, DELIVERED, ADMIN));
        assertFalse(machine.canTransition(AWAITING_PICKUP, COMPLETED, ADMIN));
        assertFalse(machine.canTransition(SHIPPING, COMPLETED, ADMIN));
    }

    @Test
    void goingBackwardIsRejected() {
        assertFalse(machine.canTransition(AWAITING_PICKUP, PENDING_CONFIRMATION, ADMIN));
        assertFalse(machine.canTransition(SHIPPING, AWAITING_PICKUP, ADMIN));
        assertFalse(machine.canTransition(DELIVERED, SHIPPING, ADMIN));
        assertFalse(machine.canTransition(COMPLETED, DELIVERED, ADMIN));
    }

    @Test
    void movingToTheSameStatusIsRejected() {
        for (OrderStatus status : OrderStatus.values()) {
            assertFalse(machine.canTransition(status, status, ADMIN), "status " + status);
        }
    }

    @Test
    void finalStatusesHaveNoWayOut() {
        for (OrderStatus from : EnumSet.of(COMPLETED, CANCELLED)) {
            assertTrue(machine.isFinal(from));
            for (OrderStatus to : OrderStatus.values()) {
                for (OrderActor actor : OrderActor.values()) {
                    assertFalse(machine.canTransition(from, to, actor),
                            from + " -> " + to + " by " + actor);
                }
            }
        }
    }

    @Test
    void nonFinalStatusesAreNotFinal() {
        assertFalse(machine.isFinal(PENDING_CONFIRMATION));
        assertFalse(machine.isFinal(AWAITING_PICKUP));
        assertFalse(machine.isFinal(SHIPPING));
        assertFalse(machine.isFinal(DELIVERED));
    }

    @Test
    void nullArgumentsAreRejected() {
        assertFalse(machine.canTransition(null, AWAITING_PICKUP, ADMIN));
        assertFalse(machine.canTransition(PENDING_CONFIRMATION, null, ADMIN));
        assertFalse(machine.canTransition(PENDING_CONFIRMATION, AWAITING_PICKUP, null));
    }

    // ---- allowedTargets ----

    @Test
    void allowedTargetsDependOnStatusAndActor() {
        assertEquals(Set.of(AWAITING_PICKUP, CANCELLED), machine.allowedTargets(PENDING_CONFIRMATION, STAFF));
        assertEquals(Set.of(CANCELLED), machine.allowedTargets(PENDING_CONFIRMATION, CUSTOMER));
        assertEquals(Set.of(SHIPPING, CANCELLED), machine.allowedTargets(AWAITING_PICKUP, ADMIN));
        assertEquals(Set.of(), machine.allowedTargets(AWAITING_PICKUP, CUSTOMER));
        assertEquals(Set.of(DELIVERED), machine.allowedTargets(SHIPPING, STAFF));
        assertEquals(Set.of(COMPLETED), machine.allowedTargets(DELIVERED, CUSTOMER));
        assertEquals(Set.of(), machine.allowedTargets(COMPLETED, ADMIN));
    }

    // ---- transition(...) ----

    @Test
    void transitionChangesStatusOfOrder() {
        Order order = orderIn(PENDING_CONFIRMATION);

        Order result = machine.transition(order, AWAITING_PICKUP, STAFF);

        assertSame(order, result);
        assertEquals(AWAITING_PICKUP, order.getStatus());
    }

    @Test
    void fullHappyPathReachesCompleted() {
        Order order = orderIn(PENDING_CONFIRMATION);

        machine.transition(order, AWAITING_PICKUP, STAFF);
        machine.transition(order, SHIPPING, STAFF);
        machine.transition(order, DELIVERED, ADMIN);
        machine.transition(order, COMPLETED, CUSTOMER);

        assertEquals(COMPLETED, order.getStatus());
    }

    @Test
    void illegalTransitionThrowsAndKeepsStatus() {
        Order order = orderIn(PENDING_CONFIRMATION);

        IllegalOrderTransitionException ex = assertThrows(IllegalOrderTransitionException.class,
                () -> machine.transition(order, SHIPPING, ADMIN));

        assertEquals(PENDING_CONFIRMATION, order.getStatus());
        assertEquals(PENDING_CONFIRMATION, ex.getFrom());
        assertEquals(SHIPPING, ex.getTo());
        assertEquals(ADMIN, ex.getActor());
    }

    @Test
    void wrongActorThrowsAndKeepsStatus() {
        Order order = orderIn(AWAITING_PICKUP);

        assertThrows(IllegalOrderTransitionException.class,
                () -> machine.transition(order, CANCELLED, CUSTOMER));

        assertEquals(AWAITING_PICKUP, order.getStatus());
    }

    @Test
    void transitionRejectsNullOrder() {
        assertThrows(IllegalArgumentException.class,
                () -> machine.transition(null, AWAITING_PICKUP, STAFF));
    }
}
