package com.misc.shafagh.orchestration;

import java.math.BigDecimal;

public record OpenLoanCommand(Long personId, Long depositId, BigDecimal collateralAmount, BigDecimal loanAmount) {
}
