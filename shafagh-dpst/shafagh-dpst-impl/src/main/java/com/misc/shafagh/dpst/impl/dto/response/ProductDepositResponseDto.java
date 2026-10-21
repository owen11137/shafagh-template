package com.misc.shafagh.dpst.impl.dto.response;

import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.dpst.impl.entity.DepositType;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** ProductDeposit: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class ProductDepositResponseDto {
    private Long id;
    private String title;
    private String code;
    private String description;
    private ProductStatus status;
    private DepositType depositType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
