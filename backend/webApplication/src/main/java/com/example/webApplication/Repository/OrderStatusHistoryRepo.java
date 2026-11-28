package com.example.webApplication.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.OrderStatusHistory;

public interface OrderStatusHistoryRepo extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtAscIdAsc(long orderId);
}
