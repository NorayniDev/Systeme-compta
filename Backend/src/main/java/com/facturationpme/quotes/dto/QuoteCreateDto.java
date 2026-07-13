package com.facturationpme.quotes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuoteCreateDto(
    @NotNull UUID clientId,
    @NotNull LocalDate issueDate,
    @NotNull LocalDate validUntil,
    @NotEmpty @Valid List<QuoteLineDto> lines,
    String notes) {}
