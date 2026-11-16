package com.example.webApplication.Entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.*;

@Entity
@Table(
    name = "cart_items", 
    uniqueConstraints = @UniqueConstraint(name = "uk_cart_variant", columnNames = {"cart_id", "product_variant_id"}),
    check = @CheckConstraint(name = "check_quantity", constraint = "quantity >")
)

public class CartItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private int quantity;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
}