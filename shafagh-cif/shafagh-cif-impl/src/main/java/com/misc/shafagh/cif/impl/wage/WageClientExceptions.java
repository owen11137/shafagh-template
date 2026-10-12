package com.misc.shafagh.cif.impl.wage;

public class WageClientExceptions extends RuntimeException {
    public WageClientExceptions(Throwable cause) { super(cause); }
    public String getErrorCode() { return "WAGE_INTEGRATION_ERROR"; }
}
