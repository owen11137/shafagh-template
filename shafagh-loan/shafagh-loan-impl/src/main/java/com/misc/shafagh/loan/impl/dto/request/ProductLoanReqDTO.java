package com.misc.shafagh.loan.impl.dto.request;

import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.loan.impl.entity.LoanType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** ProductLoan: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class ProductLoanReqDTO {
    private String title;
    private String code;
    private String description;
    private LoanType productType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private ProductStatus status;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private BigDecimal minFine;
    private BigDecimal maxFine;
    private Integer installmentInterval;
    private Integer minInstallmentCount;
    private Integer maxInstallmentCount;
    private Boolean earlySettlementAllowed;
}
