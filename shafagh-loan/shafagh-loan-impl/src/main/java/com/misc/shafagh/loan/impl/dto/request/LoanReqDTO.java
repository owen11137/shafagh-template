package com.misc.shafagh.loan.impl.dto.request;

import com.misc.shafagh.loan.impl.entity.LoanStatus;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Loan: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class LoanReqDTO {
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
}
