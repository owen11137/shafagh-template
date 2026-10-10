package com.misc.shafagh.base.impl;

/**
 * اسکیمای Oracle ماژول اطلاعات پایه و مشترک.
 * نام اسکیما فقط همین‌جا نوشته می‌شود؛ Entityها و sequenceها و (در صورت نیاز) native queryهای
 * همین ماژول به BaseSchema.NAME ارجاع می‌دهند.
 */
public final class BaseSchema {

    public static final String NAME = "SHFQ_BASE";

    private BaseSchema() {
    }
}
