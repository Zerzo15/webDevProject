package com.example.webApplication.Service.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;

@Component
public class FixedAmountDiscountPolicy implements DiscountPolicy {

    @Override
    public VoucherType type() {
        return VoucherType.FIXED_AMOUNT;
    }

    @Override
    public DiscountResult calculate(Voucher voucher, BigDecimal subtotal, BigDecimal shippingFee) {
        return DiscountResult.onGoods(voucher.getDiscountValue());
    }
}
