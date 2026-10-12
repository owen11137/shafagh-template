package com.misc.shafagh.base.impl.dto.response.branch;

import lombok.Getter;
import lombok.Setter;

/** BranchAddress: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class BranchAddressResponseDto {
    private Long id;
    private Long branchEntityId;
    private Long cityId;
    private String mainStreet;
    private String street;
    private String alley;
    private String tag;
    private String postalCode;
    private String unit;
}
