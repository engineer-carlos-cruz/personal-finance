package com.ccruz.personal_finance.transfer.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferUpsertRequest(
    @NotNull
    Long sourceAccountId,

    @NotNull
    Long targetAccountId,

    @NotNull
    @Digits(integer = 17, fraction = 2)
    BigDecimal amount,

    @NotNull
    LocalDate date
) {
}
