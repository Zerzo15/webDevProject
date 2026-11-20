package com.example.webApplication.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.OrderItem;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdOrderByIdAsc(long orderId);
}
