package com.misc.shafagh.loan.impl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductLoanNotFoundException extends RuntimeException {
    public ProductLoanNotFoundException() { super("محصول تسهیلات پیدا نشد"); }
}
