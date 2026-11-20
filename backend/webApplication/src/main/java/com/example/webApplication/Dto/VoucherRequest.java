package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.VoucherType;

/**
 * Body for creating or updating a voucher. maxDiscountAmount only matters for PERCENT vouchers.
 * minOrderAmount defaults to 0 and active defaults to true when not sent.
 */
public record VoucherRequest(
        String code,
        VoucherType type,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        Instant startDate,
        Instant endDate,
        Integer usageLimit,
        Boolean active) {
}
