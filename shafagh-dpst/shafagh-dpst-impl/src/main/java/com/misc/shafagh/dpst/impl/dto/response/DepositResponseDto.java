package com.misc.shafagh.dpst.impl.dto.response;

import com.misc.shafagh.dpst.impl.entity.DepositStatus;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** Deposit: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class DepositResponseDto {
    private Long id;
    private String depositNumber;
    private String productCode;
    private DepositStatus depositStatus;
    private BigDecimal balance;
    private String description;
    private long customerId;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
