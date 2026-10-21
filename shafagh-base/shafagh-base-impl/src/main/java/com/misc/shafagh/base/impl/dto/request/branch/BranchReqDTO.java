package com.misc.shafagh.base.impl.dto.request.branch;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Branch: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class BranchReqDTO {
    private String code;
    private String name;
    private Long fundId;
    private LocalDateTime toDate;
}
