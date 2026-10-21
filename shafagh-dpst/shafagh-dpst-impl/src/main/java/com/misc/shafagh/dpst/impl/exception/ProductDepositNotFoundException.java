package com.misc.shafagh.dpst.impl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductDepositNotFoundException extends RuntimeException {
    public ProductDepositNotFoundException() { super("محصول سپرده پیدا نشد"); }
}
