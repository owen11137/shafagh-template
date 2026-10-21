package com.misc.shafagh.dpst.impl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidProductDepositException extends RuntimeException {
    public InvalidProductDepositException(String message) { super(message); }
}
