package com.example.webApplication.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.CartDto;
import com.example.webApplication.Dto.CartItemRequest;
import com.example.webApplication.Service.CartService;
import com.example.webApplication.Service.CurrentUserService;

/**
 * The cart belongs to customers only (proposal 4.3). The owner always comes from the token.
 */
@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUser;

    public CartController(CartService cartService, CurrentUserService currentUser) {
        this.cartService = cartService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(currentUser.require(authentication)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(Authentication authentication, @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(
                cartService.addItem(currentUser.require(authentication), request.variantId(), request.quantity()));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItem(Authentication authentication, @PathVariable long itemId,
            @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(
                cartService.updateQuantity(currentUser.require(authentication), itemId, request.quantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItem(Authentication authentication, @PathVariable long itemId) {
        return ResponseEntity.ok(cartService.removeItem(currentUser.require(authentication), itemId));
    }

    @DeleteMapping
    public ResponseEntity<CartDto> clear(Authentication authentication) {
        return ResponseEntity.ok(cartService.clear(currentUser.require(authentication)));
    }
}
