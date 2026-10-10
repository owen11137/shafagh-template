package com.misc.shafagh.cif.impl.exception.business;

import com.misc.shafagh.cif.impl.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RequestNotFoundException extends BaseBusinessException {

    public RequestNotFoundException() {
        super();
        this.code = "misc.exceptions.business.4001";
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
