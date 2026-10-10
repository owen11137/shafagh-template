package com.misc.shafagh.cif.impl.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerContactReqDTO {
    private Long id;
    private Long fundId;
    private String phoneNumber;
    private String mobileNumber;
}