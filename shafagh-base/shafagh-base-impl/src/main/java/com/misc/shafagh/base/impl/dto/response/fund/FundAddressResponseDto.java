package com.misc.shafagh.base.impl.dto.response.fund;

import lombok.Getter;
import lombok.Setter;

/** FundAddress: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class FundAddressResponseDto {
    private Long id;
    private Long fundEntityId;
    private Long cityId;
    private String mainStreet;
    private String street;
    private String alley;
    private String tag;
    private String postalCode;
    private String unit;
}
