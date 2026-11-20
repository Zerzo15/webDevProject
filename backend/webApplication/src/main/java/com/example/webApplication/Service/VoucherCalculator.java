package com.example.webApplication.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.example.webApplication.Dto.PriceBreakdown;
import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Exception.InvalidVoucherException;
import com.example.webApplication.Exception.InvalidVoucherException.Reason;

/**
 * Voucher rules and money calculation (proposal 6.3). Pure logic, no database access.
 * All amounts use BigDecimal with scale 2 and HALF_UP rounding.
 * One voucher per order; usage per user is not modelled by the Voucher entity.
 */
@Component
public class VoucherCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /** unitPrice x quantity, scale 2. */
    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return money(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    /**
     * Checks that the voucher can be applied to an order with this subtotal at this moment.
     *
     * @throws InvalidVoucherException with the reason why not
     */
    public void validate(Voucher voucher, BigDecimal subtotal, Instant now) {
        if (!voucher.isActive()) {
            throw new InvalidVoucherException(Reason.INACTIVE, "Voucher is not active");
        }
        if (now.isBefore(voucher.getStartDate())) {
            throw new InvalidVoucherException(Reason.NOT_STARTED, "Voucher is not valid yet");
        }
        if (now.isAfter(voucher.getEndDate())) {
            throw new InvalidVoucherException(Reason.EXPIRED, "Voucher has expired");
        }
        if (subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new InvalidVoucherException(Reason.BELOW_MINIMUM_ORDER,
                    "Order is below the minimum amount of the voucher");
        }
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new InvalidVoucherException(Reason.USAGE_LIMIT_REACHED, "Voucher has no uses left");
        }
    }

    /**
     * Calculates the money details of an order. With a null voucher nothing is discounted.
     * A given voucher is validated first.
     *
     * PERCENT       discount = subtotal x percent / 100, capped by maxDiscountAmount (when set).
     * FIXED_AMOUNT  discount = discountValue.
     * FREESHIP      shippingDiscount = discountValue, or the whole shipping fee when discountValue is 0.
     * The discount never exceeds the subtotal and the shipping discount never exceeds the shipping fee.
     */
    public PriceBreakdown calculate(Voucher voucher, BigDecimal subtotal, BigDecimal shippingFee, Instant now) {
        BigDecimal sub = money(subtotal);
        BigDecimal fee = money(shippingFee);
        BigDecimal discount = money(BigDecimal.ZERO);
        BigDecimal shippingDiscount = money(BigDecimal.ZERO);

        if (voucher != null) {
            validate(voucher, sub, now);
            switch (voucher.getType()) {
                case PERCENT -> {
                    discount = sub.multiply(voucher.getDiscountValue()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                    if (voucher.getMaxDiscountAmount() != null) {
                        discount = discount.min(voucher.getMaxDiscountAmount());
                    }
                }
                case FIXED_AMOUNT -> discount = voucher.getDiscountValue();
                case FREESHIP -> shippingDiscount = voucher.getDiscountValue().signum() == 0
                        ? fee
                        : voucher.getDiscountValue();
            }
            discount = money(discount.min(sub));
            shippingDiscount = money(shippingDiscount.min(fee));
        }

        BigDecimal total = sub.subtract(discount).add(fee).subtract(shippingDiscount);
        return new PriceBreakdown(sub, discount, fee, shippingDiscount, money(total));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
