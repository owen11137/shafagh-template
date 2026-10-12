package com.misc.shafagh.cif.impl.exception;

/**
 * زمانی پرتاب می‌شود که تلاش شود از طریق سرویس اشخاص حقیقی روی رکورد حقوقی
 * (یا برعکس) عملیاتی انجام شود، یا personType ارسالی برای آن سرویس معتبر نباشد.
 */
public class InvalidPersonTypeException extends RuntimeException {

    public InvalidPersonTypeException(String message) {
        super(message);
    }
}
