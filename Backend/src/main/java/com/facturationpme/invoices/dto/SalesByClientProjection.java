package com.facturationpme.invoices.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Projection JPQL agregeant les factures par client - support du rapport Ventes par client. */
public record SalesByClientProjection(
    UUID clientId,
    String clientName,
    Long invoiceCount,
    BigDecimal amountExclTax,
    BigDecimal taxAmount,
    BigDecimal totalAmount) {}
