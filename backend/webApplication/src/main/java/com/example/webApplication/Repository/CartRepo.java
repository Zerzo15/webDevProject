package com.example.webApplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.Cart;

public interface CartRepo extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(long userId);
}
