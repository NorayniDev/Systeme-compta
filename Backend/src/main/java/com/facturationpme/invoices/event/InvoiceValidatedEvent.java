package com.facturationpme.invoices.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Publie une seule fois, quand une facture quitte DRAFT (que ce soit via validate ou send - les
 * deux partagent la meme transition unique). Ecoute par le module accounting pour generer les
 * ecritures de partie double ; invoices ne connait pas l'existence de ce consommateur.
 */
public record InvoiceValidatedEvent(
    UUID invoiceId,
    String invoiceNumber,
    String clientName,
    LocalDate issueDate,
    BigDecimal amountExclTax,
    BigDecimal taxAmount,
    BigDecimal totalAmount) {}
