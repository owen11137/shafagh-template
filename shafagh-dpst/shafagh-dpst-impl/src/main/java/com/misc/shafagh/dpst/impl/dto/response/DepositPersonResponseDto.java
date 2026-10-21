package com.misc.shafagh.dpst.impl.dto.response;

import com.misc.shafagh.dpst.impl.entity.DepositPersonType;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** DepositPerson: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class DepositPersonResponseDto {
    private Long id;
    private DepositPersonType depositPersonType;
    private Long depositId;
    private long customerId;
    private String description;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
