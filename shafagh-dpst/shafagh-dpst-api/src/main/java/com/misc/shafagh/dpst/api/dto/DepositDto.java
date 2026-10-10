package com.misc.shafagh.dpst.api.dto;

import java.math.BigDecimal;

public record DepositDto(Long id, Long personId, String currencyCode, BigDecimal balance, BigDecimal blockedAmount) {
}
