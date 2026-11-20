package com.example.webApplication.Exception;

/**
 * Thrown when a variant does not have enough stock for the requested quantity.
 */
public class OutOfStockException extends RuntimeException {

    private final long variantId;
    private final int requested;

    public OutOfStockException(long variantId, int requested) {
        super("Not enough stock for variant " + variantId + " (requested " + requested + ")");
        this.variantId = variantId;
        this.requested = requested;
    }

    public long getVariantId() {
        return variantId;
    }

    public int getRequested() {
        return requested;
    }
}
