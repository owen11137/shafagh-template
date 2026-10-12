package com.misc.shafagh.base.impl.dto.response.branch;

import com.misc.shafagh.common.contact.ContactType;
import lombok.Getter;
import lombok.Setter;

/** BranchContact: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class BranchContactResponseDto {
    private Long id;
    private ContactType contactType;
    private String contactNumber;
    private int regionCode;
    private String internal;
    private Long branchId;
}
