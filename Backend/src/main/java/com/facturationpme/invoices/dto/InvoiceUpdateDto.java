package com.facturationpme.invoices.dto;

import com.facturationpme.invoices.domain.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceUpdateDto(
    @NotNull UUID clientId,
    @NotNull LocalDate issueDate,
    @NotNull LocalDate dueDate,
    @NotEmpty @Valid List<InvoiceLineDto> lines,
    String notes,
    @NotNull InvoiceStatus status) {}
