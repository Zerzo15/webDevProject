package com.example.webApplication.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;

public interface OrderRepo extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Optional<Order> findByIdAndUserId(long id, long userId);

    /**
     * Orders that are in {@code status} and entered that status at or before {@code before}
     * (according to the status history).
     */
    @Query("select o from Order o where o.status = :status and exists ("
            + "select 1 from OrderStatusHistory h where h.order = o and h.toStatus = :status and h.changedAt <= :before)")
    List<Order> findInStatusSince(@Param("status") OrderStatus status, @Param("before") Instant before);
}
