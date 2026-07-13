package com.facturationpme.accounting.domain;

/** Compte du plan comptable - donnee fixe (pas de CRUD), voir {@link ChartOfAccounts}. */
public record Account(String code, String name, AccountType type) {}
