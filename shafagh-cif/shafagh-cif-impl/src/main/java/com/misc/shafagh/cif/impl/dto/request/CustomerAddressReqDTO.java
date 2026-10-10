package com.misc.shafagh.cif.impl.dto.request;

import com.misc.shafagh.core.entity.enums.AddressType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAddressReqDTO {

    private Long id;
    private Long fundId;
    private Long cityId;
    private AddressType addressType;
    private String address;
    private String postalCode;
}
