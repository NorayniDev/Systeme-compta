package com.facturationpme.accounting.dto;

import java.math.BigDecimal;

/**
 * Resultat brut de l'agregation JPQL par compte - la balance (debit - credit) se calcule ensuite.
 */
public record TrialBalanceProjection(
    String accountCode, String accountName, BigDecimal totalDebit, BigDecimal totalCredit) {}
