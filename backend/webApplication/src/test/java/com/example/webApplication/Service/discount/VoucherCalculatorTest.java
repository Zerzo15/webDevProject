package com.example.webApplication.Service.discount;

import static com.example.webApplication.Entity.VoucherType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;
import com.example.webApplication.Exception.VoucherNotApplicableException;
import com.example.webApplication.Exception.VoucherNotApplicableException.Reason;

class VoucherCalculatorTest {

    private static final Instant NOW = Instant.parse("2026-12-01T00:00:00Z");

    private final VoucherCalculator calculator = new VoucherCalculator(
        List.of(new PercentDiscountPolicy(), new FixedAmountDiscountPolicy(), new FreeShipDiscountPolicy()),
        Clock.fixed(NOW, ZoneOffset.UTC));

    // --- Cach tinh tung loai ---

    @Test
    void percentDiscountsGoodsOnly() {
        assertResult(voucher(PERCENT, "10"), "200000", "30000", "20000", "0");
    }

    @Test
    void percentIsCappedByMaxDiscount() {
        Voucher voucher = voucher(PERCENT, "50");
        voucher.setMaxDiscountAmount(money("40000"));
        assertResult(voucher, "200000", "30000", "40000", "0");
    }

    @Test
    void percentRoundsHalfUpToWholeDong() {
        // 15% cua 99.990 = 14.998,5
        assertResult(voucher(PERCENT, "15"), "99990", "0", "14999", "0");
    }

    @Test
    void fixedAmountNeverExceedsSubtotal() {
        assertResult(voucher(FIXED_AMOUNT, "50000"), "200000", "30000", "50000", "0");
        assertResult(voucher(FIXED_AMOUNT, "50000"), "30000", "30000", "30000", "0");
    }

    @Test
    void freeShipNeverExceedsShippingFee() {
        assertResult(voucher(FREESHIP, "20000"), "200000", "30000", "0", "20000");
        assertResult(voucher(FREESHIP, "999999"), "200000", "30000", "0", "30000");
    }

    // --- Dieu kien ap dung ---

    @Test
    void rejectsInactiveVoucher() {
        Voucher voucher = voucher(PERCENT, "10");
        voucher.setActive(false);
        assertRejected(voucher, "200000", Reason.VOUCHER_INACTIVE);
    }

    @Test
    void rejectsSoftDeletedVoucher() {
        Voucher voucher = voucher(PERCENT, "10");
        voucher.setDeletedAt(NOW.minusSeconds(1));
        assertRejected(voucher, "200000", Reason.VOUCHER_INACTIVE);
    }

    @Test
    void checksValidityPeriodIncludingBothEnds() {
        Voucher notStarted = voucher(PERCENT, "10");
        notStarted.setStartDate(NOW.plusSeconds(1));
        assertRejected(notStarted, "200000", Reason.VOUCHER_NOT_STARTED);

        Voucher expired = voucher(PERCENT, "10");
        expired.setEndDate(NOW.minusSeconds(1));
        assertRejected(expired, "200000", Reason.VOUCHER_EXPIRED);

        Voucher edge = voucher(PERCENT, "10");
        edge.setStartDate(NOW);
        edge.setEndDate(NOW);
        assertResult(edge, "200000", "0", "20000", "0");
    }

    @Test
    void rejectsWhenUsageLimitReached() {
        Voucher voucher = voucher(PERCENT, "10");
        voucher.setUsageLimit(5);
        voucher.setUsedCount(5);
        assertRejected(voucher, "200000", Reason.VOUCHER_USAGE_LIMIT_REACHED);
    }

    @Test
    void requiresMinimumOrderAmount() {
        Voucher voucher = voucher(PERCENT, "10");
        voucher.setMinOrderAmount(money("100000"));
        assertRejected(voucher, "99999", Reason.VOUCHER_MIN_ORDER_NOT_MET);
        assertResult(voucher, "100000", "0", "10000", "0");
    }

    @Test
    void rejectsNegativeMoney() {
        assertThatThrownBy(() -> calculator.calculate(voucher(PERCENT, "10"), money("-1"), money("0")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private void assertResult(Voucher voucher, String subtotal, String fee,
                              String discount, String shippingDiscount) {
        DiscountResult result = calculator.calculate(voucher, money(subtotal), money(fee));
        assertThat(result.discountAmount()).isEqualByComparingTo(discount);
        assertThat(result.shippingDiscount()).isEqualByComparingTo(shippingDiscount);
    }

    private void assertRejected(Voucher voucher, String subtotal, Reason reason) {
        assertThatThrownBy(() -> calculator.calculate(voucher, money(subtotal), money("30000")))
            .isInstanceOfSatisfying(VoucherNotApplicableException.class,
                e -> assertThat(e.getReason()).isEqualTo(reason));
    }

    private static Voucher voucher(VoucherType type, String value) {
        Voucher voucher = new Voucher();
        voucher.setType(type);
        voucher.setDiscountValue(money(value));
        voucher.setStartDate(NOW.minusSeconds(3600));
        voucher.setEndDate(NOW.plusSeconds(3600));
        return voucher;
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
