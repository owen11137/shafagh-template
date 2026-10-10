package com.misc.shafagh.cif.impl.exception.externalService;

import com.misc.client.infra.starter.v3.exception.handler.BaseBusinessException;

/**
 * @author m_kor
 * date 12/1/2025
 */
public class ShahkarServiceIsDisableException extends BaseBusinessException {
    public ShahkarServiceIsDisableException() {
        super();
        super.code = "misc.exceptions.business.4001";
    }

    @Override
    public String getErrorCode() {
        return code;
    }
}
