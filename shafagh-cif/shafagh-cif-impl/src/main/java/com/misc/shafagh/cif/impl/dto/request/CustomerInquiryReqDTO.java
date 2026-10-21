package com.misc.shafagh.cif.impl.dto.request;

import com.misc.shafagh.cif.impl.entity.enums.CustomerInquiryStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import lombok.Getter;
import lombok.Setter;

/** CustomerInquiry: ورودی ایجاد/ویرایش؛ شناسه رکورد از مسیر سرویس تعیین می‌شود. */
@Getter
@Setter
public class CustomerInquiryReqDTO {
    private CustomerType customerType;
    private Long customerId;
    private String response;
    private Integer responseValue;
    private String responseHeader;
    private CustomerInquiryStatus customerInquiryStatus;
    private Long customerContactId;
}
