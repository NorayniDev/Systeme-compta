package com.facturationpme.invoices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Projection JPQL des factures en retard - support du rapport Creances echues. */
public record AgingReceivableProjection(
    UUID invoiceId,
    String invoiceNumber,
    String clientName,
    LocalDate dueDate,
    BigDecimal amountDue) {}
