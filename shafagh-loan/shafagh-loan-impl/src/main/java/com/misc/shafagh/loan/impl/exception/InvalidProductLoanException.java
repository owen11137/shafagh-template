package com.misc.shafagh.loan.impl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidProductLoanException extends RuntimeException {
    public InvalidProductLoanException(String message) { super(message); }
}
