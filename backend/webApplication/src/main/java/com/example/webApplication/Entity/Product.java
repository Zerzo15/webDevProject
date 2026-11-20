package com.example.webApplication.Entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(
    name = "products", 
    indexes = {
        @Index(name = "idx_products_category_id", columnList = "category_id")},
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

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public int getWeightGrams() { return weightGrams; }
    public void setWeightGrams(int weightGrams) { this.weightGrams = weightGrams; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }
}