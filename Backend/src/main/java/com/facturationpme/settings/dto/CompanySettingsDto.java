package com.facturationpme.settings.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Meme forme en lecture et en ecriture, alignee sur {@code ICompanySettings} (aucun id expose). */
public record CompanySettingsDto(
    @NotBlank String companyName,
    @NotBlank String address,
    @NotBlank String taxId,
    @NotBlank String currency,
    @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal defaultTaxRate,
    @NotBlank String invoicePrefix,
    @NotBlank String quotePrefix) {}
