package com.misc.shafagh.cif.impl.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureImageDto {

    private Long signatureId;
    private byte[] image;
}
