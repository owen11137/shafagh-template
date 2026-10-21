package com.misc.shafagh.loan.impl.dto.request.installment;

import com.misc.shafagh.loan.impl.entity.installment.InstallmentStatus;
import com.misc.shafagh.loan.impl.entity.installment.InstallmentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Installment: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class InstallmentReqDTO {
    private InstallmentType installmentType;
    private Long loanId;
    private Integer installmentNumber;
    private BigDecimal installmentAmount;
    private LocalDateTime installmentDate;
    private InstallmentStatus installmentStatus;
}
