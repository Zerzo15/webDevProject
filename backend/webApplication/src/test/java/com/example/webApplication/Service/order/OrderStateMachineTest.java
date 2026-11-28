package com.example.webApplication.Service.order;

import static com.example.webApplication.Entity.OrderStatus.*;
import static com.example.webApplication.Entity.UserRoleStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.OrderStatusHistory;
import com.example.webApplication.Event.OrderStatusChangedEvent;
import com.example.webApplication.Exception.InvalidOrderTransitionException;
import com.example.webApplication.Exception.OrderTransitionForbiddenException;
import com.example.webApplication.Repository.OrderStatusHistoryRepo;

class OrderStateMachineTest {

    private final OrderStatusHistoryRepo historyRepo = mock(OrderStatusHistoryRepo.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final OrderStateMachine machine =
        new OrderStateMachine(new OrderTransitionRules(), historyRepo, events);

    @Test
    void changesStatusRecordsHistoryAndPublishesEvent() {
        Order order = orderIn(PENDING_CONFIRMATION);

        machine.transition(order, AWAITING_PICKUP, STAFF, "staff@example.test", "Da xac nhan");

        assertThat(order.getStatus()).isEqualTo(AWAITING_PICKUP);

        ArgumentCaptor<OrderStatusHistory> history = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(historyRepo).save(history.capture());
        assertThat(history.getValue().getOrder()).isSameAs(order);
        assertThat(history.getValue().getFromStatus()).isEqualTo(PENDING_CONFIRMATION);
        assertThat(history.getValue().getToStatus()).isEqualTo(AWAITING_PICKUP);
        assertThat(history.getValue().getChangedBy()).isEqualTo("staff@example.test");
        assertThat(history.getValue().getReason()).isEqualTo("Da xac nhan");

        verify(events).publishEvent(new OrderStatusChangedEvent(
            order.getId(), PENDING_CONFIRMATION, AWAITING_PICKUP, "staff@example.test"));
    }

    @Test
    void rejectsAPathThatDoesNotExistAndChangesNothing() {
        Order order = orderIn(SHIPPING);

        assertThatThrownBy(() -> machine.transition(order, CANCELLED, STAFF, "staff", null))
            .isInstanceOf(InvalidOrderTransitionException.class);

        assertThat(order.getStatus()).isEqualTo(SHIPPING);
        verify(historyRepo, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void rejectsTheWrongRoleAndChangesNothing() {
        Order order = orderIn(PENDING_CONFIRMATION);

        assertThatThrownBy(() -> machine.transition(order, AWAITING_PICKUP, CUSTOMER, "khach", null))
            .isInstanceOf(OrderTransitionForbiddenException.class);

        assertThat(order.getStatus()).isEqualTo(PENDING_CONFIRMATION);
        verify(historyRepo, never()).save(any());
        verifyNoInteractions(events);
    }

    private static Order orderIn(OrderStatus status) {
        Order order = new Order();
        order.setStatus(status);
        return order;
    }
}
