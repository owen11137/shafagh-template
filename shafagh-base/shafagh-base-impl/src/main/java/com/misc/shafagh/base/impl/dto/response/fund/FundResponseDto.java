package com.misc.shafagh.base.impl.dto.response.fund;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Fund: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class FundResponseDto {
    private Long id;
    private String code;
    private String name;
    private Long ownerId;
    private LocalDateTime toDate;
}
