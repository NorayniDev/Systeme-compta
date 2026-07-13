package com.facturationpme.invoices.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceCreateDto(
    @NotNull UUID clientId,
    @NotNull LocalDate issueDate,
    @NotNull LocalDate dueDate,
    @NotEmpty @Valid List<InvoiceLineDto> lines,
    String notes) {}
