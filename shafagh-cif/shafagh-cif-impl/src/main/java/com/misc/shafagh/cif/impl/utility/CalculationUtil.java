package com.misc.shafagh.cif.impl.utility;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author m_kor
 * date 8/3/2025
 */
public class CalculationUtil {
    public static BigDecimal calculateTermFeeFormulaAmount(BigDecimal feeRateDayPercent, BigDecimal remainedAmount, BigDecimal term, BigDecimal index, int remainDay) {
        return BigDecimalUtil.Round.Normal(remainedAmount.multiply(new BigDecimal(remainDay), MathContext.DECIMAL128).multiply(feeRateDayPercent, MathContext.DECIMAL128));
    }
}

