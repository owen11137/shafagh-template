package com.misc.shafagh.cif.impl.utility;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author m_kor
 * date 8/2/2025
 */
public class BigDecimalUtil {
    public final static int Equal = 0;
    public final static int Less = -1;
    public final static int Greater = 1;
    public final static int Negative = -1;
    public final static int Positive = 1;

    public static BigDecimal getMin(BigDecimal value1, BigDecimal value2) {
        int compare = value1.compareTo(value2);
        if (compare < 0)
            return value1;
        else
            return value2;
    }

    public static BigDecimal getMax(BigDecimal value1, BigDecimal value2) {
        int compare = value1.compareTo(value2);
        if (compare < 0)
            return value2;
        else
            return value1;
    }

    public static BigDecimal subtract(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) amount1 = BigDecimal.ZERO;
        if (amount2 == null) amount2 = BigDecimal.ZERO;
        return amount1.subtract(amount2, MathContext.DECIMAL128);
    }

    public static BigDecimal subtract(BigDecimal amount1, BigDecimal[] amount2) {
        if (amount1 == null) amount1 = BigDecimal.ZERO;
        if (amount2 == null) return amount1;
        for (BigDecimal amount : amount2)
            amount1 = amount1.subtract(amount, MathContext.DECIMAL128);

        return amount1;
    }

    public static BigDecimal add(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) amount1 = BigDecimal.ZERO;
        if (amount2 == null) amount2 = BigDecimal.ZERO;
        return amount1.add(amount2, MathContext.DECIMAL128);
    }

    public static BigDecimal getValue(Object amount) {
        return getValue(amount, BigDecimal.ZERO);
    }

    public static BigDecimal getValue(Object amount, BigDecimal defaultValue) {
        if (amount == null) return defaultValue;
        try {
            return new BigDecimal(amount.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultValue;
        }
    }

    public static boolean isInteger(BigDecimal value) {
        return value != null && value.stripTrailingZeros().scale() <= 0;
    }

    public static boolean IsPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == BigDecimalUtil.Greater;
    }

    public static boolean IsNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == BigDecimalUtil.Less;
    }

    public static class Round {
        public static BigDecimal Up(BigDecimal value, int scale) {
            return value.setScale(scale, RoundingMode.UP);
        }

        public static BigDecimal Down(BigDecimal value, int scale) {
            return value.setScale(scale, RoundingMode.DOWN);
        }

        public static BigDecimal Up(BigDecimal value) {
            return Up(value, 0);
        }

        public static BigDecimal Down(BigDecimal value) {
            return Down(value, 0);
        }

        public static BigDecimal Up(double value) {
            return Up(new BigDecimal(value, MathContext.DECIMAL128));
        }

        public static BigDecimal Down(double value) {
            return Down(new BigDecimal(value, MathContext.DECIMAL128));
        }

        public static BigDecimal Up(double value, int scale) {
            return Up(new BigDecimal(value, MathContext.DECIMAL128), scale);
        }

        public static BigDecimal Down(double value, int scale) {
            return Down(new BigDecimal(value, MathContext.DECIMAL128), scale);
        }

        public static BigDecimal Up(String value) {
            return Up(new BigDecimal(value, MathContext.DECIMAL128));
        }

        public static BigDecimal Down(String value) {
            return Down(new BigDecimal(value, MathContext.DECIMAL128));
        }

        public static BigDecimal Up(String value, int scale) {
            return Up(new BigDecimal(value, MathContext.DECIMAL128), scale);
        }

        public static BigDecimal Down(String value, int scale) {
            return Down(new BigDecimal(value, MathContext.DECIMAL128), scale);
        }

        public static BigDecimal Normal(BigDecimal value) {
            return NormalWithScale(value, 0);
        }

        /**
         * @param value
         * @return default scale is 2
         */
        public static BigDecimal NormalWithScale(BigDecimal value) {
            return NormalWithScale(value, 2);
        }

        public static BigDecimal NormalWithScale(BigDecimal value, int scale) {
            return value.setScale(scale, RoundingMode.HALF_UP);
        }

    }

}
