package com.ccruz.personal_finance.transfer.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferResponse(
    Long id,
    Long sourceAccountId,
    String sourceAccountCode,
    Long targetAccountId,
    String targetAccountCode,
    BigDecimal amount,
    LocalDate date
) {
}
