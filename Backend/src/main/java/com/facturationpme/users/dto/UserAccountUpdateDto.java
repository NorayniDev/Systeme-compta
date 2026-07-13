package com.facturationpme.users.dto;

import com.facturationpme.users.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserAccountUpdateDto(
    @NotBlank @Size(min = 2) String firstName,
    @NotBlank @Size(min = 2) String lastName,
    @NotBlank @Email String email,
    @NotNull UserRole role,
    boolean isActive) {}
