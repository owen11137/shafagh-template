package com.misc.shafagh.cif.impl.exception.transaction;

import com.misc.shafagh.cif.impl.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author m_kor
 * date 12/27/2025
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TransactionRelationshipNotFoundException  extends BaseBusinessException {
    public TransactionRelationshipNotFoundException() {
        super();
        super.code = "misc.exceptions.business.4001";
    }
    @Override
    public String getErrorCode() {
        return code;
    }
}
