package com.facturationpme.clients.dto;

import com.facturationpme.clients.domain.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientUpdateDto(
    @NotBlank String name,
    @NotBlank @Email String email,
    String phone,
    String address,
    String taxId,
    @NotNull ClientStatus status) {}
