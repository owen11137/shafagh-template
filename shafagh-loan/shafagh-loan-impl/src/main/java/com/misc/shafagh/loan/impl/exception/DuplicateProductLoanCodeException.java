package com.misc.shafagh.loan.impl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateProductLoanCodeException extends RuntimeException {
    public DuplicateProductLoanCodeException() { super("کد محصول تسهیلات تکراری است"); }
}
