package com.facturationpme.invoices.dto;

import java.math.BigDecimal;

/** Aligne sur {@code IInvoiceLine} (features/invoices/models/invoice.model.ts). */
public record InvoiceLineResponse(
    String id,
    String description,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal taxRate,
    BigDecimal lineTotal) {}
