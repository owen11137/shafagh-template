package com.misc.shafagh.cif.impl.utility;

public class ShafaghReportUtil {
    /**
     * @param input
     * @return String for convert latin number to persian number
     */
    public static String toPersianNumbers(String input) {
        if (input == null) return "";
        return input.replace("0", "۰")
                .replace("1", "۱")
                .replace("2", "۲")
                .replace("3", "۳")
                .replace("4", "۴")
                .replace("5", "۵")
                .replace("6", "۶")
                .replace("7", "۷")
                .replace("8", "۸")
                .replace("9", "۹");
    }
    public static String formatNumberWithComma(String number) {
        if (number == null || number.trim().isEmpty()) {
            return "";
        }

        try {
            // تبدیل به عدد (برای پشتیبانی از اعشار هم بهتره BigDecimal باشه)
            java.math.BigDecimal value = new java.math.BigDecimal(number.trim());

            // جدا کردن سه‌رقمی
            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###.##");

            String formatted = formatter.format(value);

            // تبدیل به عدد فارسی
            return ShafaghReportUtil.toPersianNumbers(formatted);

        } catch (Exception e) {
            // اگر ورودی عدد نبود
            return number;
        }
    }

}
