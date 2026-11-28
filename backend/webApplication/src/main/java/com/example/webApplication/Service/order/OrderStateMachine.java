package com.example.webApplication.Service.order;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.OrderStatusHistory;
import com.example.webApplication.Entity.UserRoleStatus;
import com.example.webApplication.Event.OrderStatusChangedEvent;
import com.example.webApplication.Exception.InvalidOrderTransitionException;
import com.example.webApplication.Exception.OrderTransitionForbiddenException;
import com.example.webApplication.Repository.OrderStatusHistoryRepo;

/**
 * Noi DUY NHAT duoc doi trang thai don hang.
 * Khong kiem tra don thuoc ve ai va khong tra kho: viec do cua service goi no.
 */
@Service
public class OrderStateMachine {

    private final OrderTransitionRules rules;
    private final OrderStatusHistoryRepo historyRepo;
    private final ApplicationEventPublisher events;

    public OrderStateMachine(
            OrderTransitionRules rules,
            OrderStatusHistoryRepo historyRepo,
            ApplicationEventPublisher events) {
        this.rules = rules;
        this.historyRepo = historyRepo;
        this.events = events;
    }

    /**
     * MANDATORY: nguoi goi phai dang mo giao dich va vua doc order trong giao dich do,
     * de trang thai, lich su (va sau nay la tra kho) cung thanh cong hoac cung rollback.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void transition(Order order, OrderStatus to, UserRoleStatus role,
                           String changedBy, String reason) {
        OrderStatus from = order.getStatus();

        // 1. Kiem tra truoc, chua sua gi
        if (!rules.exists(from, to)) {
            throw new InvalidOrderTransitionException(from, to);
        }
        if (!rules.isAllowed(from, to, role)) {
            throw new OrderTransitionForbiddenException(from, to, role);
        }

        // 2. Doi trang thai va ghi lich su
        order.setStatus(to);
        historyRepo.save(new OrderStatusHistory(order, from, to, changedBy, reason));

        // 3. Bao cho ben quan tam (vi du email), khong goi thang
        events.publishEvent(new OrderStatusChangedEvent(order.getId(), from, to, changedBy));
    }
}
