package com.misc.shafagh.dpst.impl.dto.request;

import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.dpst.impl.entity.DepositType;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** ProductDeposit: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class ProductDepositReqDTO {
    @Size(max=255)
    private String title;
    @NotBlank @Size(max=255)
    private String code;
    @Size(max=255)
    private String description;
    @NotNull
    private ProductStatus status;
    @NotNull
    private DepositType depositType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
