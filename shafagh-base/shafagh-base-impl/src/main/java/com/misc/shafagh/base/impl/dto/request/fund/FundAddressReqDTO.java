package com.misc.shafagh.base.impl.dto.request.fund;

import lombok.Getter;
import lombok.Setter;

/** FundAddress: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class FundAddressReqDTO {
    private Long fundEntityId;
    private Long cityId;
    private String mainStreet;
    private String street;
    private String alley;
    private String tag;
    private String postalCode;
    private String unit;
}
