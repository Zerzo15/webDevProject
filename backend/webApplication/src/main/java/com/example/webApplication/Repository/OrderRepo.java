package com.example.webApplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.Order;

public interface OrderRepo extends JpaRepository<Order, Long>{
    
}
