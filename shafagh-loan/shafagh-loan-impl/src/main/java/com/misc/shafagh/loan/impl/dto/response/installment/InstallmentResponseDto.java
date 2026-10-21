package com.misc.shafagh.loan.impl.dto.response.installment;

import com.misc.shafagh.loan.impl.entity.installment.InstallmentStatus;
import com.misc.shafagh.loan.impl.entity.installment.InstallmentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** Installment: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class InstallmentResponseDto {
    private Long id;
    private InstallmentType installmentType;
    private Long loanId;
    private int installmentNumber;
    private BigDecimal installmentAmount;
    private LocalDateTime installmentDate;
    private InstallmentStatus installmentStatus;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
