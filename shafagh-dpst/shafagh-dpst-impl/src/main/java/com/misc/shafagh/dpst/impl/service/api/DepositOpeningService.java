package com.misc.shafagh.dpst.impl.service.api;
import com.misc.shafagh.dpst.impl.dto.request.OpenDepositRequest;
import com.misc.shafagh.dpst.impl.dto.response.OpenDepositResponse;
/** قرارداد داخلی افتتاح حساب سپرده؛ پیاده‌سازی در service.impl قرار دارد. */
public interface DepositOpeningService {
    OpenDepositResponse open(OpenDepositRequest request);
}
