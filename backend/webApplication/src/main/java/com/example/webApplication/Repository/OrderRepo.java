package com.example.webApplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.webApplication.Entity.Order;

public interface OrderRepo extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Optional<Order> findByIdAndUserId(long id, long userId);
}
