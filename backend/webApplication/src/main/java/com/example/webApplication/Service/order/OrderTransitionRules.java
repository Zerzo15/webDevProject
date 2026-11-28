package com.example.webApplication.Service.order;

import static com.example.webApplication.Entity.OrderStatus.*;
import static com.example.webApplication.Entity.UserRoleStatus.*;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.UserRoleStatus;

/** Bang chuyen trang thai don hang (proposal 6.1). Chi tra loi cau hoi, khong doi du lieu. */
@Component
public class OrderTransitionRules {

    // from -> (to -> cac vai tro duoc phep). Cap nao khong co trong bang thi bi cam.
    private final Map<OrderStatus, Map<OrderStatus, Set<UserRoleStatus>>> table =
        new EnumMap<>(OrderStatus.class);

    public OrderTransitionRules() {
        allow(PENDING_CONFIRMATION, AWAITING_PICKUP, EnumSet.of(STAFF, ADMIN));
        allow(PENDING_CONFIRMATION, CANCELLED, EnumSet.of(CUSTOMER, STAFF, ADMIN));
        allow(AWAITING_PICKUP, SHIPPING, EnumSet.of(STAFF, ADMIN));
        allow(AWAITING_PICKUP, CANCELLED, EnumSet.of(STAFF, ADMIN));
        allow(SHIPPING, DELIVERED, EnumSet.of(STAFF, ADMIN));
        allow(DELIVERED, COMPLETED, EnumSet.of(CUSTOMER));
    }

    /** Co duong di tu from sang to hay khong, chua xet vai tro. */
    public boolean exists(OrderStatus from, OrderStatus to) {
        return table.getOrDefault(from, Map.of()).containsKey(to);
    }

    /** Vai tro nay co duoc di duong do khong. */
    public boolean isAllowed(OrderStatus from, OrderStatus to, UserRoleStatus role) {
        return table.getOrDefault(from, Map.of())
            .getOrDefault(to, Set.of())
            .contains(role);
    }

    private void allow(OrderStatus from, OrderStatus to, Set<UserRoleStatus> roles) {
        table.computeIfAbsent(from, key -> new EnumMap<>(OrderStatus.class)).put(to, roles);
    }
}
