package com.facturationpme.accounting.dto;

import java.math.BigDecimal;

/** Aligne sur {@code ITrialBalanceLine} (features/accounting/models/trial-balance.model.ts). */
public record TrialBalanceLineResponse(
    String accountCode,
    String accountName,
    BigDecimal totalDebit,
    BigDecimal totalCredit,
    BigDecimal balance) {}
