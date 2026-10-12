package com.misc.shafagh.cif.impl.wage.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WageCalcResponseDto implements Serializable {
    private long totalAmount;
    private int totalCount;
    private PaymentType paymentType;
    private long wageAmount;
    private long wageOriginalAmount;
    private long wageDiscount;
}
