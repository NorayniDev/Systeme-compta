package com.facturationpme.products.dto;

import com.facturationpme.products.domain.ProductUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductCreateDto(
    @NotBlank String name,
    @NotBlank String sku,
    String description,
    String category,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal unitPrice,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal taxRate,
    @NotNull ProductUnit unit,
    @NotNull @Min(0) Integer stockQuantity) {}
