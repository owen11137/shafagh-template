package com.misc.shafagh.base.impl.dto.request;

import lombok.Getter;
import lombok.Setter;

/** City: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class CityReqDTO {
    private String name;
    private String phoneCode;
    private String code;
    private Long parentId;
}
