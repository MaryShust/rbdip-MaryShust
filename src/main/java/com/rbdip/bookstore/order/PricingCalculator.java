package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PricingCalculator {

    private static final int BULK_QUANTITY_THRESHOLD = 10;
    private static final BigDecimal BULK_DISCOUNT = new BigDecimal("0.95");
    private static final BigDecimal VIP_DISCOUNT = new BigDecimal("0.90");
    private static final BigDecimal WHOLESALE_DISCOUNT = new BigDecimal("0.85");
    private static final BigDecimal PERCENT_COUPON_DISCOUNT = new BigDecimal("0.80");
    private static final BigDecimal FLAT_COUPON_DISCOUNT = BigDecimal.TEN;
    private static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal LARGE_ORDER_DISCOUNT = new BigDecimal("0.98");
    private static final int SCALE = 2;

    private static final String VIP = "vip";
    private static final String WHOLESALE = "wholesale";
    private static final String COUPON_SAVE_TEN = "SAVE10";
    private static final String COUPON_SAVE_TWENTY_PERCENT = "SAVE20PERCENT";

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = sumLines(items);
        total = applyCustomerDiscount(total, customerType);
        total = applyCoupon(total, couponCode);
        total = clampNegative(total);
        total = applyLargeOrderDiscount(total);
        return total.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal sumLines(List<LineItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > BULK_QUANTITY_THRESHOLD) {
                linePrice = linePrice.multiply(BULK_DISCOUNT);
            }
            total = total.add(linePrice);
        }
        return total;
    }

    private BigDecimal applyCustomerDiscount(BigDecimal total, String customerType) {
        if (VIP.equals(customerType)) {
            return total.multiply(VIP_DISCOUNT);
        }
        if (WHOLESALE.equals(customerType)) {
            return total.multiply(WHOLESALE_DISCOUNT);
        }
        return total;
    }

    private BigDecimal applyCoupon(BigDecimal total, String couponCode) {
        if (COUPON_SAVE_TEN.equals(couponCode)) {
            return total.subtract(FLAT_COUPON_DISCOUNT);
        }
        if (COUPON_SAVE_TWENTY_PERCENT.equals(couponCode)) {
            return total.multiply(PERCENT_COUPON_DISCOUNT);
        }
        return total;
    }

    private BigDecimal clampNegative(BigDecimal total) {
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    private BigDecimal applyLargeOrderDiscount(BigDecimal total) {
        if (total.compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            return total.multiply(LARGE_ORDER_DISCOUNT);
        }
        return total;
    }
}
