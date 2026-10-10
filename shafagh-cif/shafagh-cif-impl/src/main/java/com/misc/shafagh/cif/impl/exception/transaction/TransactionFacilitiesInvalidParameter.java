package com.misc.shafagh.cif.impl.exception.transaction;

import com.misc.shafagh.cif.impl.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author m_kor
 * date 1/5/2026
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TransactionFacilitiesInvalidParameter  extends BaseBusinessException {
    public TransactionFacilitiesInvalidParameter() {
        super();
        super.code = "misc.exceptions.business.4001";
    }

    @Override
    public String getErrorCode() {
        return code;
    }
}
