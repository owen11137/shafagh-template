package com.misc.shafagh.base.impl.dto.response.branch;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Branch: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class BranchResponseDto {
    private Long id;
    private String code;
    private String name;
    private Long fundId;
    private LocalDateTime toDate;
}
