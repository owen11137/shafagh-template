package com.misc.shafagh.base.impl.dto.request.fund;

import com.misc.shafagh.common.contact.ContactType;
import lombok.Getter;
import lombok.Setter;

/** FundContact: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class FundContactReqDTO {
    private ContactType contactType;
    private String contactNumber;
    private Long fundEntityId;
    private Integer regionCode;
    private String internal;
    private Long branchId;
}
