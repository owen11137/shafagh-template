package com.misc.shafagh;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class QardHasanInstallmentCalculator {

    private static final BigDecimal ROUND_UNIT = BigDecimal.valueOf(1000);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public enum InstallmentType { FEE, PRINCIPAL }

    public static class Installment {
        private final int number;               // شماره قسط (ماه)
        private final InstallmentType type;     // نوع قسط
        private final BigDecimal amount;        // مبلغ قسط (ریال)

        public Installment(int number, InstallmentType type, BigDecimal amount) {
            this.number = number;
            this.type = type;
            this.amount = amount;
        }

        public int getNumber() { return number; }
        public InstallmentType getType() { return type; }
        public BigDecimal getAmount() { return amount; }

        @Override
        public String toString() {
            return "Installment{no=" + number + ", type=" + type + ", amount=" + amount.toPlainString() + "}";
        }
    }

    /**
     * @param principal       مبلغ اصل وام (ریال)
     * @param months          تعداد ماه‌های وام
     * @param annualFeeRate   نرخ سالانه کارمزد به درصد (مثلاً 4 برای ۴ درصد)
     */
    public static List<Installment> calculate(BigDecimal principal, int months, BigDecimal annualFeeRate) {
        if (principal == null || principal.signum() <= 0)
            throw new IllegalArgumentException("مبلغ وام نامعتبر است");
        if (months < 2)
            throw new IllegalArgumentException("تعداد ماه باید حداقل ۲ باشد");
        if (annualFeeRate == null || annualFeeRate.signum() < 0)
            throw new IllegalArgumentException("نرخ کارمزد نامعتبر است");

        // ماه‌های کارمزد: 1، 13، 25، ... (12n+1)
        int feeMonthsCount = (months + 11) / 12;
        int principalMonthsCount = months - feeMonthsCount;

        // ۱) قسط اصل: تقسیم مساوی و رند به ۱۰۰۰ ریال
        BigDecimal exactShare = principal.divide(BigDecimal.valueOf(principalMonthsCount), 10, RoundingMode.HALF_UP);
        BigDecimal roundedShare = exactShare.divide(ROUND_UNIT, 0, RoundingMode.HALF_UP).multiply(ROUND_UNIT);

        // ۲) مجموع اختلاف ریز و رند شده (می‌تواند منفی هم باشد) = اصل وام - مجموع اقساط رند شده
        BigDecimal diff = principal.subtract(roundedShare.multiply(BigDecimal.valueOf(principalMonthsCount)));

        // ۳) ساخت لیست اقساط
        List<Installment> result = new ArrayList<>(months);
        BigDecimal remaining = principal;
        boolean secondInstallmentAdjusted = false;

        for (int month = 1; month <= months; month++) {
            if ((month - 1) % 12 == 0) {
                // قسط کارمزد: روی مانده اصل در ابتدای سال
                BigDecimal fee = remaining.multiply(annualFeeRate)
                        .divide(HUNDRED, 0, RoundingMode.HALF_UP);
                result.add(new Installment(month, InstallmentType.FEE, fee));
            } else {
                BigDecimal amount = roundedShare;
                if (month == 2 && !secondInstallmentAdjusted) {
                    amount = amount.add(diff);   // اضافه شدن اختلاف به قسط دوم
                    secondInstallmentAdjusted = true;
                }
                remaining = remaining.subtract(amount);
                result.add(new Installment(month, InstallmentType.PRINCIPAL, amount));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<Installment> list = calculate(new BigDecimal("100000000"), 24, new BigDecimal("4"));
        list.forEach(System.out::println);
    }
}