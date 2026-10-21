package com.misc.shafagh.dpst.impl.dto.request;

import com.misc.shafagh.dpst.impl.entity.DepositStatus;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Deposit: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class DepositReqDTO {
    private String depositNumber;
    private String productCode;
    private DepositStatus depositStatus;
    private BigDecimal balance;
    private String description;
    private Long customerId;
}
