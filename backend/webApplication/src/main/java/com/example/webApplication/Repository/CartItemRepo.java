package com.example.webApplication.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.CartItem;

public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartIdOrderByIdAsc(long cartId);

    Optional<CartItem> findByCartIdAndProductVariantId(long cartId, long productVariantId);

    Optional<CartItem> findByIdAndCartId(long id, long cartId);

    void deleteByCartId(long cartId);
}
