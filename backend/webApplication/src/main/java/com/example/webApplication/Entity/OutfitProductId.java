package com.example.webApplication.Entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OutfitProductId implements Serializable {

    @Column(name = "outfit_id")
    private long outfitId;

    @Column(name = "product_id")
    private long productId;

    public long getOutfitId() {
        return outfitId;
    }

    public void setOutfitId(long outfitId) {
        this.outfitId = outfitId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OutfitProductId)) {
            return false;
        }
        OutfitProductId that = (OutfitProductId) other;
        return outfitId == that.outfitId && productId == that.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(outfitId, productId);
    }
}
