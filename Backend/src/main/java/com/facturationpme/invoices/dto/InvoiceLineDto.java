package com.facturationpme.invoices.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InvoiceLineDto(
    @NotBlank String description,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal unitPrice,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal taxRate) {}
