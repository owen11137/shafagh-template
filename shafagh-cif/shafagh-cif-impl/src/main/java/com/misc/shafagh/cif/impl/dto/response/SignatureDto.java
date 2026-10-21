package com.misc.shafagh.cif.impl.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * امضای شخص در پاسخ‌ها. image در JSON به‌صورت Base64 برمی‌گردد.
 */
@Getter
@Setter
public class SignatureDto {

    private Long id;
    private Date fromDate;
    private Date toDate;
    private Long customerId;
    private String description;
    private byte[] image;
}
