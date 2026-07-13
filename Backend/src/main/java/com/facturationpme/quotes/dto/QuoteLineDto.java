package com.facturationpme.quotes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuoteLineDto(
    @NotBlank String description,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal unitPrice,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal taxRate) {}
