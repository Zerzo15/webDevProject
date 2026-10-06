package com.example.webApplication.Entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "outfit_product")
public class OutfitProduct {
    
    @EmbeddedId
    private OutfitProductId id;

    @MapsId("outfitId")
    @ManyToOne
    @JoinColumn(name = "outfit_id", nullable = false)
    private OutfitRecommendation outfit;

    @MapsId("productId")
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public OutfitProductId getId() {
        return id;
    }

    public void setId(OutfitProductId id) {
        this.id = id;
    }

    public OutfitRecommendation getOutfit() {
        return outfit;
    }

    public void setOutfit(OutfitRecommendation outfit) {
        this.outfit = outfit;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}