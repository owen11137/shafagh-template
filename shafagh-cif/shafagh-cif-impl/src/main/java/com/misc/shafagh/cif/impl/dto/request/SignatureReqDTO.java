package com.misc.shafagh.cif.impl.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class SignatureReqDTO {

    private Long id;
    private Long fundId;
    private Date fromDate;
    private Long customerId;
    private Date toDate;
    private String description;
    private String image;
}
