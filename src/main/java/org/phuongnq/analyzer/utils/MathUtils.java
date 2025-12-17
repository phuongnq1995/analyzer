package org.phuongnq.analyzer.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtils {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public static BigDecimal toPercentageOf(BigDecimal value, BigDecimal total) {
        if (isZero(total)) {
            return BigDecimal.ZERO;
        }
        return value.divide(total, 4, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
    }

    public static BigDecimal percentOf(BigDecimal percentage, BigDecimal total) {
        return percentage.multiply(total).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public static boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }
}
