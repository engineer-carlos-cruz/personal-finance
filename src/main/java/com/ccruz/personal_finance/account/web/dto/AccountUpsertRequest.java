package com.ccruz.personal_finance.account.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountUpsertRequest(
    @NotBlank
    @Size(max = 50)
    String code,

    @NotBlank
    @Size(max = 255)
    String description,
    
    @NotNull
    @PositiveOrZero
    @Digits(integer = 17, fraction = 2)
    BigDecimal balance
) {
}
