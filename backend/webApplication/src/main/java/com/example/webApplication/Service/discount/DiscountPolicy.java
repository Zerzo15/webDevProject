package com.example.webApplication.Service.discount;

import java.math.BigDecimal;

import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;

/**
 * Cach tinh cua MOT loai voucher. Them loai moi = them mot lop moi.
 * Dieu kien ap dung, lam tron va chan tran do {@link VoucherCalculator} lo.
 */
public interface DiscountPolicy {

    VoucherType type();

    DiscountResult calculate(Voucher voucher, BigDecimal subtotal, BigDecimal shippingFee);
}
