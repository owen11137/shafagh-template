package com.misc.shafagh.dpst.impl.dto.request;

import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.dpst.impl.entity.DepositType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** ProductDeposit: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class ProductDepositReqDTO {
    private String title;
    private String code;
    private String description;
    private ProductStatus status;
    private DepositType depositType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
