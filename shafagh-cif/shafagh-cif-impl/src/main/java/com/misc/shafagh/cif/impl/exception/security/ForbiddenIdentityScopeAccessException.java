package com.misc.shafagh.cif.impl.exception.security;

import com.misc.shafagh.cif.impl.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenIdentityScopeAccessException extends BaseBusinessException {

    public ForbiddenIdentityScopeAccessException(String message) {
        super(message);
        this.code = "misc.exceptions.security.3001";
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
