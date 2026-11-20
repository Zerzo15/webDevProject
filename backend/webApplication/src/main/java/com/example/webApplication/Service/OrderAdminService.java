package com.example.webApplication.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Dto.OrderAdminFilter;
import com.example.webApplication.Dto.OrderAdminRowDto;
import com.example.webApplication.Dto.OrderDetailDto;
import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Exception.InvalidRequestException;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.OrderRepo;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

/**
 * Handling the orders of all customers, for staff and admin (proposal 4.3, 5.2, 6.1).
 * Status changes go through OrderService.changeStatus, which uses the state machine and writes the history.
 */
@Service
public class OrderAdminService {

    private final OrderRepo orderRepo;
    private final OrderService orderService;

    public OrderAdminService(OrderRepo orderRepo, OrderService orderService) {
        this.orderRepo = orderRepo;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public Page<OrderAdminRowDto> search(OrderAdminFilter filter, Pageable pageable) {
        return orderRepo.findAll(toSpecification(filter), pageable).map(order -> new OrderAdminRowDto(
                order.getId(), order.getStatus(), order.getUser().getEmail(), order.getPaymentMethod(),
                order.getTotalAmount(), order.getTrackingCode(), order.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public OrderDetailDto get(long orderId) {
        return orderService.toDetail(find(orderId));
    }

    /**
     * Moves the order to the target status as staff or admin.
     *
     * @throws com.example.webApplication.Exception.IllegalOrderTransitionException when the state machine
     *         does not allow it for this actor
     */
    @Transactional
    public OrderDetailDto transition(long orderId, OrderStatus target, OrderActor actor, String changedBy, String reason) {
        if (target == null) {
            throw new InvalidRequestException("target is required");
        }
        Order order = orderRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " does not exist"));
        String why = reason == null || reason.isBlank() ? null : reason.trim();
        orderService.changeStatus(order, target, actor, changedBy, why);
        return orderService.toDetail(find(orderId));
    }

    private Order find(long orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " does not exist"));
    }

    private static Specification<Order> toSpecification(OrderAdminFilter f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (f.status() != null) {
                ps.add(cb.equal(root.get("status"), f.status()));
            }
            if (f.createdFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), f.createdFrom()));
            }
            if (f.createdTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.<Instant>get("createdAt"), f.createdTo()));
            }
            if (f.totalFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("totalAmount"), f.totalFrom()));
            }
            if (f.totalTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("totalAmount"), f.totalTo()));
            }
            if (f.paymentMethod() != null && !f.paymentMethod().isBlank()) {
                ps.add(cb.equal(cb.lower(root.get("paymentMethod")), f.paymentMethod().trim().toLowerCase()));
            }
            if (f.keyword() != null && !f.keyword().isBlank()) {
                String keyword = f.keyword().trim().toLowerCase();
                Join<Order, User> user = root.join("user");
                Predicate byEmail = cb.like(cb.lower(user.get("email")), "%" + keyword + "%");
                if (keyword.chars().allMatch(Character::isDigit) && keyword.length() <= 18) {
                    ps.add(cb.or(cb.equal(root.get("id"), Long.parseLong(keyword)), byEmail));
                } else {
                    ps.add(byEmail);
                }
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
