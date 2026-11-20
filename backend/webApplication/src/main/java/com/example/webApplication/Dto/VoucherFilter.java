package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.VoucherType;

/**
 * The five filters of the voucher list (proposal 5.2). Every field is optional.
 * validFrom/validTo keep vouchers whose validity period overlaps that range.
 */
public record VoucherFilter(
        String code,
        VoucherType type,
        Boolean active,
        Instant validFrom,
        Instant validTo,
        BigDecimal minOrderFrom,
        BigDecimal minOrderTo) {
}
