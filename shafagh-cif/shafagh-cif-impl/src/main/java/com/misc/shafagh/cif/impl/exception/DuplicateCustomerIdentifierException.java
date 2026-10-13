package com.misc.shafagh.cif.impl.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/** Customer identifiers are unique within a fund, not across different funds. */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCustomerIdentifierException extends ValidationException {
    public DuplicateCustomerIdentifierException() { super("شناسه مشتری در صندوق جاری تکراری است"); }
}
