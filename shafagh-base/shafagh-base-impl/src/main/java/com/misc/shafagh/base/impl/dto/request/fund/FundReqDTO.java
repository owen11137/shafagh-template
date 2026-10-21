package com.misc.shafagh.base.impl.dto.request.fund;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Fund: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class FundReqDTO {
    private String code;
    private String name;
    private Long ownerId;
    private LocalDateTime toDate;
}
