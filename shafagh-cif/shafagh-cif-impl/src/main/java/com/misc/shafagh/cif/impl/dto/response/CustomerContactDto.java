package com.misc.shafagh.cif.impl.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerContactDto {
    private Long id;
    private Long fundId;
    private String mobileNumber;
    private String phoneNumber;
}
