package com.example.webApplication.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Dto.CartDto;
import com.example.webApplication.Dto.CartItemDto;
import com.example.webApplication.Entity.Cart;
import com.example.webApplication.Entity.CartItem;
import com.example.webApplication.Entity.Product;
import com.example.webApplication.Entity.ProductVariant;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Exception.InvalidRequestException;
import com.example.webApplication.Exception.OutOfStockException;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.CartItemRepo;
import com.example.webApplication.Repository.CartRepo;
import com.example.webApplication.Repository.ProductVariantRepo;

/**
 * The cart of a customer (proposal 5.1). Every method works on the cart of the user passed in,
 * which the controller takes from the token, so users cannot reach each other's carts.
 * The cart shows the current price and stock; stock is only taken when the order is placed.
 */
@Service
public class CartService {

    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final ProductVariantRepo variantRepo;

    public CartService(CartRepo cartRepo, CartItemRepo cartItemRepo, ProductVariantRepo variantRepo) {
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.variantRepo = variantRepo;
    }

    @Transactional
    public CartDto getCart(User user) {
        return toDto(cartItemRepo.findByCartIdOrderByIdAsc(getOrCreateCart(user).getId()));
    }

    /** Adds the variant to the cart; when it is already there the quantities are added together. */
    @Transactional
    public CartDto addItem(User user, Long variantId, Integer quantity) {
        if (variantId == null) {
            throw new InvalidRequestException("variantId is required");
        }
        int qty = requireQuantity(quantity);
        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant " + variantId + " does not exist"));
        requireAvailable(variant);

        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepo.findByCartIdAndProductVariantId(cart.getId(), variantId).orElse(null);
        int newQuantity = (item == null ? 0 : item.getQuantity()) + qty;
        requireStock(variant, newQuantity);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProductVariant(variant);
        }
        item.setQuantity(newQuantity);
        cartItemRepo.save(item);
        return toDto(cartItemRepo.findByCartIdOrderByIdAsc(cart.getId()));
    }

    /** Sets the quantity of a cart line. */
    @Transactional
    public CartDto updateQuantity(User user, long itemId, Integer quantity) {
        int qty = requireQuantity(quantity);
        Cart cart = getOrCreateCart(user);
        CartItem item = findItem(cart, itemId);
        requireStock(item.getProductVariant(), qty);
        item.setQuantity(qty);
        cartItemRepo.save(item);
        return toDto(cartItemRepo.findByCartIdOrderByIdAsc(cart.getId()));
    }

    @Transactional
    public CartDto removeItem(User user, long itemId) {
        Cart cart = getOrCreateCart(user);
        cartItemRepo.delete(findItem(cart, itemId));
        return toDto(cartItemRepo.findByCartIdOrderByIdAsc(cart.getId()));
    }

    @Transactional
    public CartDto clear(User user) {
        cartItemRepo.deleteByCartId(getOrCreateCart(user).getId());
        return new CartDto(List.of(), money(BigDecimal.ZERO), false);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepo.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepo.save(cart);
        });
    }

    private CartItem findItem(Cart cart, long itemId) {
        // An item of another user's cart is reported as not found, not as forbidden.
        return cartItemRepo.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item " + itemId + " does not exist"));
    }

    private static int requireQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new InvalidRequestException("quantity must be at least 1");
        }
        return quantity;
    }

    private static void requireAvailable(ProductVariant variant) {
        Product product = variant.getProduct();
        if (product == null || !product.isActive()) {
            throw new InvalidRequestException("This product is not available");
        }
    }

    private static void requireStock(ProductVariant variant, int quantity) {
        if (quantity > variant.getStockQuantity()) {
            throw new OutOfStockException(variant.getId(), quantity);
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private CartDto toDto(List<CartItem> items) {
        List<CartItemDto> lines = new ArrayList<>();
        BigDecimal subtotal = money(BigDecimal.ZERO);
        boolean problem = false;
        for (CartItem item : items) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();
            BigDecimal lineTotal = VoucherCalculator.lineTotal(product.getBasePrice(), item.getQuantity());
            boolean inStock = item.getQuantity() <= variant.getStockQuantity();
            problem = problem || !inStock || !product.isActive();
            subtotal = subtotal.add(lineTotal);
            lines.add(new CartItemDto(item.getId(), variant.getId(), product.getId(), product.getName(),
                    variant.getSize(), variant.getColor(), money(product.getBasePrice()), item.getQuantity(),
                    lineTotal, variant.getStockQuantity(), inStock));
        }
        return new CartDto(lines, subtotal, problem);
    }
}
