package com.misc.shafagh.dpst.impl.dto.request;

import com.misc.shafagh.dpst.impl.entity.DepositPersonType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** DepositPerson: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class DepositPersonReqDTO {
    private DepositPersonType depositPersonType;
    private Long depositId;
    private Long customerId;
    private String description;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
