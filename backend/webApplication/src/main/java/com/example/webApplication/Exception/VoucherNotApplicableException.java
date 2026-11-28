package com.example.webApplication.Exception;

public class VoucherNotApplicableException extends RuntimeException {

    public enum Reason {
        VOUCHER_INACTIVE,
        VOUCHER_NOT_STARTED,
        VOUCHER_EXPIRED,
        VOUCHER_USAGE_LIMIT_REACHED,
        VOUCHER_MIN_ORDER_NOT_MET
    }

    private final Reason reason;

    public VoucherNotApplicableException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
