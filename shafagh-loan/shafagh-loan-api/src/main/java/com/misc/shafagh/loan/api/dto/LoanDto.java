package com.misc.shafagh.loan.api.dto;

import java.math.BigDecimal;

public record LoanDto(Long id, Long personId, Long depositId, BigDecimal amount, LoanStatus status) {
}
