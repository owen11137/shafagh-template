package com.misc.shafagh.cif.impl.dto.response;

import com.misc.shafagh.cif.impl.entity.enums.CustomerInquiryStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/** CustomerInquiry: پاسخ عملیات؛ فاقد گراف بازگشتی Entityها. */
@Getter
@Setter
public class CustomerInquiryResponseDto {
    private Long id;
    private CustomerType customerType;
    private Long customerId;
    private String response;
    private int responseValue;
    private String responseHeader;
    private CustomerInquiryStatus customerInquiryStatus;
    private Long customerContactId;
    private Long fundId;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private Long version;
}
