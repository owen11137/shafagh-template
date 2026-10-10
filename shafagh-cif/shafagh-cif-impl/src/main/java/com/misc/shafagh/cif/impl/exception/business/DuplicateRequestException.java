package com.misc.shafagh.cif.impl.exception.business;

import com.misc.shafagh.cif.impl.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DuplicateRequestException extends BaseBusinessException {

    public DuplicateRequestException() {
        super();
        this.code = "misc.exceptions.business.4002";
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
