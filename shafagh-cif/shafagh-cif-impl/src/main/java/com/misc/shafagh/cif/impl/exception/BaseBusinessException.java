package com.misc.shafagh.cif.impl.exception;

import org.springframework.http.HttpStatus;

/** Local replacement for legacy framework exception. */
public class BaseBusinessException extends RuntimeException {
    protected String code;
    public BaseBusinessException() { super(); }
    public BaseBusinessException(String message) { super(message); }
    public BaseBusinessException(String message, Throwable cause) { super(message, cause); }
    public String getErrorCode() { return code; }
    public HttpStatus getHttpStatus() { return HttpStatus.BAD_REQUEST; }
}
