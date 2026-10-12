package com.misc.shafagh.cif.impl.wage.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class WageCreateRequestDto implements Serializable {
    private String requestId;
    private String accountId;
    private long totalAmount;
    private int totalCount;
    private int branchCode;
    private PaymentType paymentType;
}
