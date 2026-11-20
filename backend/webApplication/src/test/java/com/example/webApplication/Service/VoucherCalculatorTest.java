package com.example.webApplication.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.example.webApplication.Dto.PriceBreakdown;
import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;
import com.example.webApplication.Exception.InvalidVoucherException;
import com.example.webApplication.Exception.InvalidVoucherException.Reason;

class VoucherCalculatorTest {

    private static final Instant NOW = Instant.parse("2026-11-20T10:00:00Z");
    private static final BigDecimal SHIPPING = new BigDecimal("30000");

    private final VoucherCalculator calculator = new VoucherCalculator();

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static Voucher voucher(VoucherType type, String value) {
        Voucher v = new Voucher();
        v.setCode("TEST");
        v.setType(type);
        v.setDiscountValue(bd(value));
        v.setMinOrderAmount(BigDecimal.ZERO);
        v.setStartDate(NOW.minus(1, ChronoUnit.DAYS));
        v.setEndDate(NOW.plus(1, ChronoUnit.DAYS));
        v.setUsageLimit(10);
        v.setUsedCount(0);
        v.setActive(true);
        return v;
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual), "expected " + expected + " but was " + actual);
        assertEquals(2, actual.scale(), "scale of " + actual);
    }

    // ---- no voucher ----

    @Test
    void withoutVoucherTotalIsSubtotalPlusShipping() {
        PriceBreakdown p = calculator.calculate(null, bd("200000"), SHIPPING, NOW);

        assertMoney("200000", p.subtotal());
        assertMoney("0", p.discount());
        assertMoney("30000", p.shippingFee());
        assertMoney("0", p.shippingDiscount());
        assertMoney("230000", p.total());
    }

    // ---- PERCENT ----

    @Test
    void percentVoucherDiscountsTheSubtotal() {
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.PERCENT, "10"), bd("200000"), SHIPPING, NOW);

        assertMoney("20000", p.discount());
        assertMoney("210000", p.total());
    }

    @Test
    void percentVoucherIsCappedByMaxDiscount() {
        Voucher v = voucher(VoucherType.PERCENT, "50");
        v.setMaxDiscountAmount(bd("30000"));

        PriceBreakdown p = calculator.calculate(v, bd("200000"), SHIPPING, NOW);

        assertMoney("30000", p.discount());
        assertMoney("200000", p.total());
    }

    @Test
    void percentVoucherBelowTheCapIsNotRaisedToTheCap() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setMaxDiscountAmount(bd("50000"));

        PriceBreakdown p = calculator.calculate(v, bd("200000"), SHIPPING, NOW);

        assertMoney("20000", p.discount());
    }

    @Test
    void percentDiscountIsRoundedHalfUpToTwoDecimals() {
        // 33.33% of 100.05 = 33.3466... -> 33.35
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.PERCENT, "33.33"), bd("100.05"), BigDecimal.ZERO, NOW);

        assertMoney("33.35", p.discount());
        assertMoney("66.70", p.total());
    }

    @Test
    void hundredPercentVoucherMakesTheGoodsFree() {
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.PERCENT, "100"), bd("200000"), SHIPPING, NOW);

        assertMoney("200000", p.discount());
        assertMoney("30000", p.total());
    }

    // ---- FIXED_AMOUNT ----

    @Test
    void fixedAmountVoucherSubtractsTheValue() {
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.FIXED_AMOUNT, "50000"), bd("200000"), SHIPPING, NOW);

        assertMoney("50000", p.discount());
        assertMoney("180000", p.total());
    }

    @Test
    void fixedAmountVoucherNeverExceedsTheSubtotal() {
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.FIXED_AMOUNT, "500000"), bd("200000"), SHIPPING, NOW);

        assertMoney("200000", p.discount());
        assertMoney("30000", p.total());
    }

    // ---- FREESHIP ----

    @Test
    void freeshipWithZeroValueWaivesTheWholeShippingFee() {
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.FREESHIP, "0"), bd("200000"), SHIPPING, NOW);

        assertMoney("0", p.discount());
        assertMoney("30000", p.shippingDiscount());
        assertMoney("200000", p.total());
    }

    @Test
    void freeshipWithValueReducesTheShippingFeeByThatValue() {
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.FREESHIP, "10000"), bd("200000"), SHIPPING, NOW);

        assertMoney("10000", p.shippingDiscount());
        assertMoney("220000", p.total());
    }

    @Test
    void freeshipNeverExceedsTheShippingFee() {
        PriceBreakdown p = calculator.calculate(voucher(VoucherType.FREESHIP, "100000"), bd("200000"), SHIPPING, NOW);

        assertMoney("30000", p.shippingDiscount());
        assertMoney("200000", p.total());
    }

    // ---- validation ----

    private void assertRejected(Reason reason, Voucher v, String subtotal) {
        InvalidVoucherException ex = assertThrows(InvalidVoucherException.class,
                () -> calculator.calculate(v, bd(subtotal), SHIPPING, NOW));
        assertEquals(reason, ex.getReason());
    }

    @Test
    void inactiveVoucherIsRejected() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setActive(false);

        assertRejected(Reason.INACTIVE, v, "200000");
    }

    @Test
    void voucherThatHasNotStartedIsRejected() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setStartDate(NOW.plusSeconds(1));
        v.setEndDate(NOW.plus(2, ChronoUnit.DAYS));

        assertRejected(Reason.NOT_STARTED, v, "200000");
    }

    @Test
    void expiredVoucherIsRejected() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setStartDate(NOW.minus(2, ChronoUnit.DAYS));
        v.setEndDate(NOW.minusSeconds(1));

        assertRejected(Reason.EXPIRED, v, "200000");
    }

    @Test
    void voucherIsValidOnTheExactStartAndEndInstants() {
        Voucher v = voucher(VoucherType.FIXED_AMOUNT, "1000");
        v.setStartDate(NOW);
        v.setEndDate(NOW);

        calculator.validate(v, bd("200000"), NOW);
    }

    @Test
    void orderBelowTheMinimumIsRejected() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setMinOrderAmount(bd("300000"));

        assertRejected(Reason.BELOW_MINIMUM_ORDER, v, "299999.99");
    }

    @Test
    void orderExactlyAtTheMinimumIsAccepted() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setMinOrderAmount(bd("300000"));

        calculator.validate(v, bd("300000"), NOW);
    }

    @Test
    void voucherWithNoUsesLeftIsRejected() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setUsageLimit(5);
        v.setUsedCount(5);

        assertRejected(Reason.USAGE_LIMIT_REACHED, v, "200000");
    }

    @Test
    void voucherWithOneUseLeftIsAccepted() {
        Voucher v = voucher(VoucherType.PERCENT, "10");
        v.setUsageLimit(5);
        v.setUsedCount(4);

        calculator.validate(v, bd("200000"), NOW);
    }

    // ---- lineTotal ----

    @Test
    void lineTotalMultipliesPriceByQuantity() {
        assertMoney("750000", VoucherCalculator.lineTotal(bd("250000"), 3));
        assertMoney("59.97", VoucherCalculator.lineTotal(bd("19.99"), 3));
    }
}
