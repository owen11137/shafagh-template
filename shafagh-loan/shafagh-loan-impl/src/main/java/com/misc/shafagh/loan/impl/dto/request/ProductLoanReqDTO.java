package com.misc.shafagh.loan.impl.dto.request;

import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.loan.impl.entity.LoanType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** ProductLoan: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class ProductLoanReqDTO {
    @Size(max = 300)
    private String title;
    @NotBlank(message = "کد محصول الزامی است")
    @Size(max = 20, message = "کد محصول حداکثر ۲۰ کاراکتر است")
    private String code;
    @Size(max = 300)
    private String description;
    @NotNull(message = "نوع محصول الزامی است")
    private LoanType productType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private ProductStatus status;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private BigDecimal minAmount;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private BigDecimal maxAmount;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private BigDecimal minFee;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private BigDecimal maxFee;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private BigDecimal minFine;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private BigDecimal maxFine;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private Integer installmentInterval;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private Integer minInstallmentCount;
    @PositiveOrZero(message = "مقدار مالی یا تعداد اقساط نمی‌تواند منفی باشد")
    private Integer maxInstallmentCount;
    @NotNull(message = "امکان تسویه زودهنگام باید مشخص شود")
    private Boolean earlySettlementAllowed;
}
