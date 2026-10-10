package com.misc.shafagh.cif.impl.wage.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReversalResponseDto implements Serializable {
    private long totalAmount;
    private int totalCount;
    private long reverseAmount;
}