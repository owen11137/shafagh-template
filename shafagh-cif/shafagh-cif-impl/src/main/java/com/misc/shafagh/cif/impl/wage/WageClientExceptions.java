package com.misc.shafagh.cif.impl.wage;


import com.misc.client.infra.starter.v3.integration.BaseIntegrationException;

public class WageClientExceptions extends BaseIntegrationException {

    public WageClientExceptions(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String getErrorCode() {
        return code;
    }
}
