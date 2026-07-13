package com.facturationpme.services.dto;

import com.facturationpme.services.domain.ServiceItemUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ServiceItemCreateDto(
    @NotBlank String name,
    @NotBlank String code,
    String description,
    String category,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal unitPrice,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal taxRate,
    @NotNull ServiceItemUnit unit) {}
