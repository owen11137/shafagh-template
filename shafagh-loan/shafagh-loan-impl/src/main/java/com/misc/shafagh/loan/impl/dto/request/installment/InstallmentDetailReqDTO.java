package com.misc.shafagh.loan.impl.dto.request.installment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** InstallmentDetail: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class InstallmentDetailReqDTO {
    private Long loanId;
    private Integer installmentNumber;
    private BigDecimal paidAmount;
    private LocalDateTime paidDate;
    private BigDecimal fineAmount;
    private Long transactionNO;
}
