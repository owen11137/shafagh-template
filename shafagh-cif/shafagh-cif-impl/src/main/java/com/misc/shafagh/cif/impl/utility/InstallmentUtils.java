package com.misc.shafagh.cif.impl.utility;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author m_kor
 * date 7/29/2025
 */
public class InstallmentUtils {
    public static BigDecimal getCalculatedPercent(BigDecimal value) {
        return value.divide(new BigDecimal(100, MathContext.DECIMAL128), MathContext.DECIMAL128);
    }

    public static BigDecimal getCalculatedDailyPercent(BigDecimal value, int remainDay) {
        return value
                .multiply(getDailyPercentIndex(remainDay), MathContext.DECIMAL128)
                .divide(new BigDecimal(100, MathContext.DECIMAL128), MathContext.DECIMAL128);
    }

    public static BigDecimal getDailyPercentIndex(int remainDay) {
        if (remainDay > 0) {
            return BigDecimal.ONE.divide(new BigDecimal(remainDay, MathContext.DECIMAL128), MathContext.DECIMAL128);
        } else {
            return BigDecimal.ONE.divide(new BigDecimal(365, MathContext.DECIMAL128), MathContext.DECIMAL128);
        }

    }

    public static BigDecimal calculateInstallmentAmountForTermFeeFormula(BigDecimal loanAmount, BigDecimal term) {
        return loanAmount.divide(term.subtract(BigDecimalUtil.Round.Up(term.divide(
                new BigDecimal(12, MathContext.DECIMAL128), MathContext.DECIMAL128)), MathContext.DECIMAL128), MathContext.DECIMAL128);
    }

}
