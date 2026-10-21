package com.misc.shafagh.cif.impl.dto.response;

import com.misc.shafagh.base.api.dto.CityDto;

import com.misc.shafagh.cif.impl.entity.enums.AddressType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAddressDto {
    private Long id;
    private AddressType addressType;
    private CityDto city;
    private String address;
    private String postalCode;
}
