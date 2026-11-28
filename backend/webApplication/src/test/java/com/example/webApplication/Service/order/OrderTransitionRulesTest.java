package com.example.webApplication.Service.order;

import static com.example.webApplication.Entity.OrderStatus.*;
import static com.example.webApplication.Entity.UserRoleStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.UserRoleStatus;

class OrderTransitionRulesTest {

    private final OrderTransitionRules rules = new OrderTransitionRules();

    /** Duyet het 6 x 6 x 3 to hop: chi dung cac dong duoi day duoc phep. */
    @Test
    void allowsExactlyTheAgreedTransitions() {
        Set<String> expected = Set.of(
            key(PENDING_CONFIRMATION, AWAITING_PICKUP, STAFF),
            key(PENDING_CONFIRMATION, AWAITING_PICKUP, ADMIN),
            key(PENDING_CONFIRMATION, CANCELLED, CUSTOMER),
            key(PENDING_CONFIRMATION, CANCELLED, STAFF),
            key(PENDING_CONFIRMATION, CANCELLED, ADMIN),
            key(AWAITING_PICKUP, SHIPPING, STAFF),
            key(AWAITING_PICKUP, SHIPPING, ADMIN),
            key(AWAITING_PICKUP, CANCELLED, STAFF),
            key(AWAITING_PICKUP, CANCELLED, ADMIN),
            key(SHIPPING, DELIVERED, STAFF),
            key(SHIPPING, DELIVERED, ADMIN),
            key(DELIVERED, COMPLETED, CUSTOMER));

        Set<String> actual = new HashSet<>();
        for (OrderStatus from : OrderStatus.values()) {
            for (OrderStatus to : OrderStatus.values()) {
                for (UserRoleStatus role : UserRoleStatus.values()) {
                    if (rules.isAllowed(from, to, role)) {
                        actual.add(key(from, to, role));
                    }
                }
            }
        }

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void knowsWhetherAPathExistsRegardlessOfRole() {
        assertThat(rules.exists(DELIVERED, COMPLETED)).isTrue();
        assertThat(rules.exists(SHIPPING, CANCELLED)).isFalse();
        assertThat(rules.exists(PENDING_CONFIRMATION, SHIPPING)).isFalse();
        assertThat(rules.exists(SHIPPING, AWAITING_PICKUP)).isFalse();
    }

    private static String key(OrderStatus from, OrderStatus to, UserRoleStatus role) {
        return from + ">" + to + ":" + role;
    }
}
