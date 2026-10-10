package com.misc.shafagh.cif.impl.exception.transaction;

import com.misc.client.infra.starter.v3.exception.handler.BaseBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author m_kor
 * date 12/29/2025
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TransactionInvalidException extends BaseBusinessException {
    public TransactionInvalidException() {
        super();
        super.code = "misc.exceptions.business.4001";
    }

    @Override
    public String getErrorCode() {
        return code;
    }
}
