package com.misc.shafagh.loan.impl.dto.response;

import com.misc.shafagh.loan.impl.entity.LoanStatus;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** Loan: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class LoanResponseDto {
    private Long id;
    private String loanNumber;
    private String productCode;
    private BigDecimal amount;
    private Integer installmentCount;
    private BigDecimal feeRate;
    private BigDecimal fineRate;
    private String description;
    private LoanStatus loanStatus;
    private String grantNumber;
    private String sourceNumber;
    private BigDecimal balance;
    private BigDecimal settlementBalance;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
