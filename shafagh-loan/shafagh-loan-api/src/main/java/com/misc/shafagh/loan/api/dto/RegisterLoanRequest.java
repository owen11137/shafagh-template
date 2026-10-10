package com.misc.shafagh.loan.api.dto;

import java.math.BigDecimal;

public record RegisterLoanRequest(Long personId, Long depositId, BigDecimal amount) {
}
