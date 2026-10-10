package com.misc.shafagh.loan.impl;

/**
 * اسکیمای Oracle ماژول تسهیلات.
 * نام اسکیما فقط همین‌جا نوشته می‌شود؛ Entityها و sequenceها و (در صورت نیاز) native queryهای
 * همین ماژول به LoanSchema.NAME ارجاع می‌دهند.
 */
public final class LoanSchema {

    public static final String NAME = "SHFQ_LOAN";

    private LoanSchema() {
    }
}
