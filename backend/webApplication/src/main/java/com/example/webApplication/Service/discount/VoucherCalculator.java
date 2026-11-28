package com.example.webApplication.Service.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;
import com.example.webApplication.Exception.VoucherNotApplicableException;
import com.example.webApplication.Exception.VoucherNotApplicableException.Reason;

/**
 * Kiem tra voucher co dung duoc khong roi tinh tien giam.
 * Chi tinh, khong tang used_count va khong ghi database.
 */
@Component
public class VoucherCalculator {

    private final Map<VoucherType, DiscountPolicy> policies = new EnumMap<>(VoucherType.class);
    private final Clock clock;

    @Autowired
    public VoucherCalculator(List<DiscountPolicy> policies) {
        this(policies, Clock.systemUTC());
    }

    /** Cho test: truyen dong ho co dinh. */
    VoucherCalculator(List<DiscountPolicy> policies, Clock clock) {
        policies.forEach(policy -> this.policies.put(policy.type(), policy));
        this.clock = clock;
    }

    public DiscountResult calculate(Voucher voucher, BigDecimal subtotal, BigDecimal shippingFee) {
        requireNotNegative(subtotal, "subtotal");
        requireNotNegative(shippingFee, "shippingFee");
        checkApplicable(voucher, subtotal);

        DiscountPolicy policy = policies.get(voucher.getType());
        if (policy == null) {
            throw new IllegalStateException("No discount policy for " + voucher.getType());
        }
        DiscountResult raw = policy.calculate(voucher, subtotal, shippingFee);

        // Lam tron va chan tran o MOT cho cho moi loai voucher
        return new DiscountResult(
            roundAndCap(raw.discountAmount(), subtotal),
            roundAndCap(raw.shippingDiscount(), shippingFee));
    }

    private void checkApplicable(Voucher voucher, BigDecimal subtotal) {
        Instant now = clock.instant();

        if (!voucher.isActive() || voucher.getDeletedAt() != null) {
            throw new VoucherNotApplicableException(Reason.VOUCHER_INACTIVE);
        }
        if (now.isBefore(voucher.getStartDate())) {
            throw new VoucherNotApplicableException(Reason.VOUCHER_NOT_STARTED);
        }
        if (now.isAfter(voucher.getEndDate())) {
            throw new VoucherNotApplicableException(Reason.VOUCHER_EXPIRED);
        }
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new VoucherNotApplicableException(Reason.VOUCHER_USAGE_LIMIT_REACHED);
        }
        // compareTo, khong dung equals: 100000 va 100000.00 phai bang nhau
        if (subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new VoucherNotApplicableException(Reason.VOUCHER_MIN_ORDER_NOT_MET);
        }
    }

    private static BigDecimal roundAndCap(BigDecimal amount, BigDecimal ceiling) {
        return amount.setScale(0, RoundingMode.HALF_UP).min(ceiling);
    }

    private static void requireNotNegative(BigDecimal value, String name) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(name + " must not be null or negative");
        }
    }
}
