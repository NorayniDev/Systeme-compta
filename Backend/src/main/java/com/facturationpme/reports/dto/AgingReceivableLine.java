package com.facturationpme.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AgingReceivableLine(
    UUID invoiceId,
    String invoiceNumber,
    String clientName,
    LocalDate dueDate,
    long daysOverdue,
    BigDecimal amountDue) {}
