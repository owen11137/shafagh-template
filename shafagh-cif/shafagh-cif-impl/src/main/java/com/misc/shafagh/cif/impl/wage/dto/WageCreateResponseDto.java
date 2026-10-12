package com.misc.shafagh.cif.impl.wage.dto;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class WageCreateResponseDto extends WageCalcResponseDto implements Serializable {
    private String requestId;
    private WageStatus requestStatus;
    private String accountId;
    private Date creationDate;
    private Date submitDate;
    private boolean reversed;
    private Date reverseDate;
    private ReversalResponseDto reversal;
}
