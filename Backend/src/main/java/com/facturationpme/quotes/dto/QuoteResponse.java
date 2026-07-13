package com.facturationpme.quotes.dto;

import com.facturationpme.quotes.domain.QuoteStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Aligne sur {@code IQuote} (features/quotes/models/quote.model.ts). */
public record QuoteResponse(
    String id,
    String number,
    String clientId,
    String clientName,
    LocalDate issueDate,
    LocalDate validUntil,
    List<QuoteLineResponse> lines,
    BigDecimal amountExclTax,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    QuoteStatus status,
    String notes,
    String convertedInvoiceId,
    Instant createdAt,
    Instant updatedAt) {}
