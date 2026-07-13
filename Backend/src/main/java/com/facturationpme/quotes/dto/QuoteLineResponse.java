package com.facturationpme.quotes.dto;

import java.math.BigDecimal;

/** Aligne sur {@code IQuoteLine} (features/quotes/models/quote.model.ts). */
public record QuoteLineResponse(
    String id,
    String description,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal taxRate,
    BigDecimal lineTotal) {}
