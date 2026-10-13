package com.misc.shafagh.dpst.impl.dto.response;
import java.math.BigDecimal;
import com.misc.shafagh.dpst.impl.entity.DepositStatus;
/** نتیجه افتتاح؛ شماره حساب داخلی است و شماره شبا محسوب نمی‌شود. */
public record OpenDepositResponse(Long id, String depositNumber, Long customerId,
        String productCode, DepositStatus status, BigDecimal balance) {}
