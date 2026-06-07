package com.ccruz.personal_finance.account.web.dto;

import java.math.BigDecimal;

public record AccountResponse(
    Long id,
    String code,
    String description,
    BigDecimal balance
) {
}
