package com.facturationpme.suppliers.dto;

import com.facturationpme.suppliers.domain.SupplierStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupplierUpdateDto(
    @NotBlank String name,
    @NotBlank @Email String email,
    String phone,
    String address,
    String taxId,
    @NotNull SupplierStatus status) {}
