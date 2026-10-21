package com.misc.shafagh.loan.impl.dto.response.installment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** InstallmentDetail: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class InstallmentDetailResponseDto {
    private Long id;
    private Long loanId;
    private int installmentNumber;
    private BigDecimal paidAmount;
    private LocalDateTime paidDate;
    private BigDecimal fineAmount;
    private long transactionNO;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
