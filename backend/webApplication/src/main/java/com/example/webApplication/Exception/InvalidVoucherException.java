package com.example.webApplication.Exception;

/**
 * Thrown when a voucher cannot be applied to an order.
 */
public class InvalidVoucherException extends RuntimeException {

    public enum Reason {
        NOT_FOUND,
        INACTIVE,
        NOT_STARTED,
        EXPIRED,
        BELOW_MINIMUM_ORDER,
        USAGE_LIMIT_REACHED
    }

    private final Reason reason;

    public InvalidVoucherException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
