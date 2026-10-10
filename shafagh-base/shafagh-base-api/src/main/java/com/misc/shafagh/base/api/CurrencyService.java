package com.misc.shafagh.base.api;

import com.misc.shafagh.base.api.dto.CurrencyDto;

/** سرویس expose شده‌ی ماژول SHFQ_BASE برای سایر ماژول‌ها. */
public interface CurrencyService {

    CurrencyDto create(CurrencyDto currency);

    CurrencyDto getByCode(String code);

    boolean exists(String code);
}
