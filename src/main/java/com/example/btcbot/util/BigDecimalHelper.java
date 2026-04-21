package com.example.btcbot.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BigDecimalHelper {

    private BigDecimalHelper() {
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(value));
    }

    public static BigDecimal roundDown(BigDecimal value, BigDecimal step) {
        if (step == null || step.compareTo(BigDecimal.ZERO) <= 0) {
            return value;
        }
        return value.divide(step, 0, RoundingMode.DOWN).multiply(step).stripTrailingZeros();
    }

    public static BigDecimal volatilityPercent(BigDecimal high, BigDecimal low, BigDecimal current) {
        if (current == null || current.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return high.subtract(low)
                .divide(current, 16, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
