package com.misc.shafagh.cif.impl.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityDto {

    private Long id;
    private String name;
    private String code;
    private String phoneCode;
    private String province;
    private Long parentId;
}
