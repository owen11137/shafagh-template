package com.misc.shafagh.dpst.impl;

/**
 * اسکیمای Oracle ماژول سپرده.
 * نام اسکیما فقط همین‌جا نوشته می‌شود؛ Entityها و sequenceها و (در صورت نیاز) native queryهای
 * همین ماژول به DpstSchema.NAME ارجاع می‌دهند.
 */
public final class DpstSchema {

    public static final String NAME = "SHFQ_DPST";

    private DpstSchema() {
    }
}
