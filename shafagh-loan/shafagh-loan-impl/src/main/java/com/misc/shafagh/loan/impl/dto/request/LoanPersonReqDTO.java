package com.misc.shafagh.loan.impl.dto.request;

import com.misc.shafagh.loan.impl.entity.CollateralType;
import com.misc.shafagh.loan.impl.entity.LoanPersonType;
import lombok.Getter;
import lombok.Setter;

/** LoanPerson: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class LoanPersonReqDTO {
    private Long customerId;
    private Long loanId;
    private LoanPersonType loanPersonType;
    private CollateralType collateralType;
    private String collateralNumber;
    private String description;
}
