package org.phuongnq.analyzer.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtils {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public static float toPercentageOf(BigDecimal value, BigDecimal total) {
        return toFloat(toPercentage(value, total));
    }

    public static float toPercentageOf(int value, int total) {
        return toFloat(toPercentage(BigDecimal.valueOf(value), BigDecimal.valueOf(total)));
    }

    public static BigDecimal toPercentage(BigDecimal value, BigDecimal total) {
        if (isZero(total)) {
            return BigDecimal.ZERO;
        }
        return value.divide(total, 4, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
    }

    public static float toFloat(BigDecimal original) {
        BigDecimal roundedBigDecimal = original.setScale(2, RoundingMode.HALF_UP);
        return roundedBigDecimal.floatValue();
    }

    public static boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}
