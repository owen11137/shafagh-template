package com.misc.shafagh.loan.impl.dto.response;

import com.misc.shafagh.loan.impl.entity.CollateralType;
import com.misc.shafagh.loan.impl.entity.LoanPersonType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** LoanPerson: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class LoanPersonResponseDto {
    private Long id;
    private Long customerId;
    private Long loanId;
    private LoanPersonType loanPersonType;
    private CollateralType collateralType;
    private String collateralNumber;
    private String description;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
