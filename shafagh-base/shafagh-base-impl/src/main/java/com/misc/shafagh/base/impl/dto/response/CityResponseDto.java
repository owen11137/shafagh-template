package com.misc.shafagh.base.impl.dto.response;

import lombok.Getter;
import lombok.Setter;

/** City: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class CityResponseDto {
    private Long id;
    private String name;
    private String phoneCode;
    private String code;
    private Long parentId;
}
