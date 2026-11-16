package com.example.webApplication.Entity;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(
    name = "products", 
    indexes = {
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_slug", columnList = "slug")},
    check = @CheckConstraint(constraint = "base_price >= 0 AND weight_grams >= 0")
    )
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String material;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "weight_grams", nullable = false)
    private int weightGrams = 0;

    // Reference to Cloud Storage bucket URL
    @Column(name = "image_url", length = 500)
    private String imageUrl; 

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}