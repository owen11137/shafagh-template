package com.misc.shafagh.base.impl.dto.request.branch;

import com.misc.shafagh.common.contact.ContactType;
import lombok.Getter;
import lombok.Setter;

/** BranchContact: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class BranchContactReqDTO {
    private ContactType contactType;
    private String contactNumber;
    private Integer regionCode;
    private String internal;
    private Long branchId;
}
