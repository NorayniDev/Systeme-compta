package com.facturationpme.clients.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientCreateDto(
    @NotBlank String name,
    @NotBlank @Email String email,
    String phone,
    String address,
    String taxId) {}
