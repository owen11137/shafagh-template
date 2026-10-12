package com.misc.shafagh.base.impl.dto.request.branch;

import lombok.Getter;
import lombok.Setter;

/** BranchAddress: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class BranchAddressReqDTO {
    private Long branchEntityId;
    private Long cityId;
    private String mainStreet;
    private String street;
    private String alley;
    private String tag;
    private String postalCode;
    private String unit;
}
