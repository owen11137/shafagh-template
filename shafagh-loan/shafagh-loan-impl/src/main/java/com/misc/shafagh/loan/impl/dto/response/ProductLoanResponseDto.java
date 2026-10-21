package com.misc.shafagh.loan.impl.dto.response;

import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.loan.impl.entity.LoanType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** ProductLoan: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class ProductLoanResponseDto {
    private Long id;
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
    private int installmentInterval;
    private int minInstallmentCount;
    private int maxInstallmentCount;
    private Boolean earlySettlementAllowed;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
