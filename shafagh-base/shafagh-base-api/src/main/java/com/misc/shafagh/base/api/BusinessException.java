package com.misc.shafagh.base.api;

/**
 * استثنای کسب‌وکاری مشترک بین همه‌ی ماژول‌ها.
 * RuntimeException است تا به‌صورت پیش‌فرض باعث rollback تراکنش شود.
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
