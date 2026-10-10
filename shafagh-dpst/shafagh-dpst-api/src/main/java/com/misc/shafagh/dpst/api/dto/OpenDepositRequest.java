package com.misc.shafagh.dpst.api.dto;

import java.math.BigDecimal;

public record OpenDepositRequest(Long personId, String currencyCode, BigDecimal initialBalance) {
}
