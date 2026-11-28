package com.example.webApplication.Service.discount;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;

/** discountValue la so tien giam toi da cho phi ship. Muon mien han thi dat so lon. */
@Component
public class FreeShipDiscountPolicy implements DiscountPolicy {

    @Override
    public VoucherType type() {
        return VoucherType.FREESHIP;
    }

    @Override
    public DiscountResult calculate(Voucher voucher, BigDecimal subtotal, BigDecimal shippingFee) {
        return DiscountResult.onShipping(voucher.getDiscountValue());
    }
}
