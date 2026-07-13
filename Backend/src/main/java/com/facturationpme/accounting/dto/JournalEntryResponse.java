package com.facturationpme.accounting.dto;

import com.facturationpme.accounting.domain.JournalSource;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Aligne sur {@code IJournalEntry} (features/accounting/models/journal-entry.model.ts). */
public record JournalEntryResponse(
    String id,
    LocalDate date,
    String reference,
    String accountCode,
    String accountName,
    String label,
    BigDecimal debit,
    BigDecimal credit,
    JournalSource source) {}
