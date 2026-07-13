package com.facturationpme.invoices.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Projection JPQL support du graphique de revenu mensuel du tableau de bord. */
public record InvoiceMonthlyAmountProjection(LocalDate issueDate, BigDecimal totalAmount) {}
