package com.misc.shafagh.base.impl.dto.response.fund;

import com.misc.shafagh.common.contact.ContactType;
import lombok.Getter;
import lombok.Setter;

/** FundContact: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class FundContactResponseDto {
    private Long id;
    private ContactType contactType;
    private String contactNumber;
    private Long fundEntityId;
    private int regionCode;
    private String internal;
    private Long branchId;
}
