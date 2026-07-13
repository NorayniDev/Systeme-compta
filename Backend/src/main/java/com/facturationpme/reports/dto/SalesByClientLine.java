package com.facturationpme.reports.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesByClientLine(
    UUID clientId,
    String clientName,
    Long invoiceCount,
    BigDecimal amountExclTax,
    BigDecimal taxAmount,
    BigDecimal totalAmount) {}
