package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingCalculatorCharacterizationTest {

    private final PricingCalculator calculator = new PricingCalculator();

    private static PricingCalculator.LineItem item(String price, int quantity) {
        return new PricingCalculator.LineItem(new BigDecimal(price), quantity);
    }

    private BigDecimal total(String customerType, String couponCode, PricingCalculator.LineItem... items) {
        return calculator.calculateOrderTotal(List.of(items), customerType, couponCode);
    }

    @Test
    void emptyOrderCostsNothing() {
        assertThat(total("regular", null, new PricingCalculator.LineItem[0])).isEqualByComparingTo("0.00");
    }

    @Test
    void regularCustomerPaysFullPrice() {
        assertThat(total("regular", null, item("10.00", 3))).isEqualByComparingTo("30.00");
    }

    @Test
    void nullCustomerTypeIsTreatedAsRegular() {
        assertThat(total(null, null, item("10.00", 2))).isEqualByComparingTo("20.00");
    }

    @Test
    void quantityOfExactlyTenGetsNoBulkDiscount() {
        assertThat(total("regular", null, item("10.00", 10))).isEqualByComparingTo("100.00");
    }

    @Test
    void quantityAboveTenGetsBulkDiscount() {
        assertThat(total("regular", null, item("10.00", 11))).isEqualByComparingTo("104.50");
    }

    @Test
    void vipCustomerGetsTenPercentOff() {
        assertThat(total("vip", null, item("50.00", 2))).isEqualByComparingTo("90.00");
    }

    @Test
    void wholesaleCustomerGetsFifteenPercentOff() {
        assertThat(total("wholesale", null, item("50.00", 2))).isEqualByComparingTo("85.00");
    }

    @Test
    void saveTenCouponSubtractsFlatAmount() {
        assertThat(total("regular", "SAVE10", item("30.00", 1))).isEqualByComparingTo("20.00");
    }

    @Test
    void saveTwentyPercentCouponAppliesPercentage() {
        assertThat(total("regular", "SAVE20PERCENT", item("50.00", 1))).isEqualByComparingTo("40.00");
    }

    @Test
    void unknownCouponIsIgnored() {
        assertThat(total("regular", "UNKNOWN", item("50.00", 1))).isEqualByComparingTo("50.00");
    }

    @Test
    void negativeTotalIsClampedToZero() {
        assertThat(total("regular", "SAVE10", item("5.00", 1))).isEqualByComparingTo("0.00");
    }

    @Test
    void exactlyZeroTotalStaysZero() {
        assertThat(total("regular", "SAVE10", item("10.00", 1))).isEqualByComparingTo("0.00");
    }

    @Test
    void totalOfExactlyThousandGetsNoLargeOrderDiscount() {
        assertThat(total("regular", null, item("500.00", 2))).isEqualByComparingTo("1000.00");
    }

    @Test
    void totalAboveThousandGetsLargeOrderDiscount() {
        assertThat(total("regular", null, item("600.00", 2))).isEqualByComparingTo("1176.00");
    }

    @Test
    void belowThousandDoesNotGetLargeOrderDiscount() {
        assertThat(total("regular", null, item("100.00", 1))).isEqualByComparingTo("100.00");
    }

    @Test
    void discountsComposeInExpectedOrder() {
        assertThat(total("vip", "SAVE20PERCENT", item("1000.00", 2))).isEqualByComparingTo("1411.20");
    }

    @Test
    void resultIsRoundedHalfUpToTwoDecimals() {
        assertThat(total("regular", null, item("0.333", 3))).isEqualByComparingTo("1.00");
    }

    @Test
    void largeOrderDiscountAppliesAfterCoupon() {
        assertThat(total("regular", "SAVE10", item("600.00", 2))).isEqualByComparingTo("1166.20");
    }

    @Test
    void multipleLinesAreSummed() {
        assertThat(total("regular", null, item("10.00", 1), item("20.00", 2)))
                .isEqualByComparingTo("50.00");
    }
}
