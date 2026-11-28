package com.example.webApplication.Service.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;

@Component
public class PercentDiscountPolicy implements DiscountPolicy {

    @Override
    public VoucherType type() {
        return VoucherType.PERCENT;
    }

    @Override
    public DiscountResult calculate(Voucher voucher, BigDecimal subtotal, BigDecimal shippingFee) {
        // chia 100 bang cach doi dau phay: chinh xac, khong lo so chia khong het
        BigDecimal amount = subtotal.multiply(voucher.getDiscountValue()).movePointLeft(2);

        BigDecimal max = voucher.getMaxDiscountAmount();
        if (max != null) {
            amount = amount.min(max);
        }
        return DiscountResult.onGoods(amount);
    }
}
