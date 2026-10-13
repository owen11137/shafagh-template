package com.misc.shafagh.dpst.impl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateProductDepositCodeException extends RuntimeException {
    public DuplicateProductDepositCodeException() { super("کد محصول سپرده تکراری است"); }
}
