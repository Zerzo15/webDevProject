package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;

public record VoucherDto(
        long id,
        String code,
        VoucherType type,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        Instant startDate,
        Instant endDate,
        int usageLimit,
        int usedCount,
        boolean active,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy,
        Instant deletedAt,
        String deletedBy) {

    public static VoucherDto from(Voucher v) {
        return new VoucherDto(v.getId(), v.getCode(), v.getType(), v.getDiscountValue(), v.getMaxDiscountAmount(),
                v.getMinOrderAmount(), v.getStartDate(), v.getEndDate(), v.getUsageLimit(), v.getUsedCount(),
                v.isActive(), v.getCreatedAt(), v.getCreatedBy(), v.getUpdatedAt(), v.getUpdatedBy(),
                v.getDeletedAt(), v.getDeletedBy());
    }
}
