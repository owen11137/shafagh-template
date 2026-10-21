package com.misc.shafagh.dpst.impl.exception;
/** تغییر کد یا حذف محصول دارای حساب، ارتباط حساب‌ها را مخدوش می‌کند. */
public class ProductDepositInUseException extends RuntimeException {
    public ProductDepositInUseException() { super("محصول دارای حساب سپرده است؛ حذف یا تغییر کد مجاز نیست"); }
}
