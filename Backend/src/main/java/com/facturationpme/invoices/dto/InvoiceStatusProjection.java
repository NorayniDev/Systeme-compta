package com.facturationpme.invoices.dto;

import com.facturationpme.invoices.domain.InvoiceStatus;
import java.time.LocalDate;

/**
 * Projection JPQL support de la repartition par statut du tableau de bord - {@code dueDate} permet
 * de reclasser en OVERDUE a la volee (statut jamais persiste, voir InvoiceService).
 */
public record InvoiceStatusProjection(InvoiceStatus status, LocalDate dueDate) {}
