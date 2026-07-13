package com.facturationpme.accounting.dto;

import com.facturationpme.accounting.domain.AccountType;

/** Aligne sur {@code IAccount} (features/accounting/models/account.model.ts). */
public record AccountResponse(String code, String name, AccountType type) {}
