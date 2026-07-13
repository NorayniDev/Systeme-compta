package com.facturationpme.invoices.dto;

import com.facturationpme.invoices.domain.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Aligne sur {@code IInvoice} (features/invoices/models/invoice.model.ts). */
public record InvoiceResponse(
    String id,
    String number,
    String clientId,
    String clientName,
    LocalDate issueDate,
    LocalDate dueDate,
    List<InvoiceLineResponse> lines,
    BigDecimal amountExclTax,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    InvoiceStatus status,
    String notes,
    Instant createdAt,
    Instant updatedAt) {

  public InvoiceResponse withStatus(InvoiceStatus newStatus) {
    return new InvoiceResponse(
        id,
        number,
        clientId,
        clientName,
        issueDate,
        dueDate,
        lines,
        amountExclTax,
        taxAmount,
        totalAmount,
        newStatus,
        notes,
        createdAt,
        updatedAt);
  }
}
