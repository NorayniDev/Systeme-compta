package com.facturationpme.payments.dto;

import com.facturationpme.payments.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCreateDto(
    @NotNull UUID invoiceId,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
    @NotNull PaymentMethod method,
    @NotNull Instant paidAt,
    String notes) {}
