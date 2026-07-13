package com.facturationpme.accounting.domain;

import java.util.List;

/**
 * Plan comptable fixe de l'application (5 comptes, suffisants pour le cycle facturation ->
 * encaissement). Pas de table ni de CRUD : ces comptes sont une donnee de reference du code, au
 * meme titre que {@code RolePermissions} pour les roles - reproduit fidelement le plan utilise par
 * le mode demo frontend ({@code core/mocks/mock-data.ts::mockAccounts}).
 */
public final class ChartOfAccounts {

  public static final Account CLIENT = new Account("411", "Clients", AccountType.ASSET);
  public static final Account BANK = new Account("512", "Banque", AccountType.ASSET);
  public static final Account CASH = new Account("531", "Caisse", AccountType.ASSET);
  public static final Account SALES =
      new Account("706", "Prestations de services", AccountType.REVENUE);
  public static final Account VAT_COLLECTED =
      new Account("44571", "TVA collectee", AccountType.LIABILITY);

  public static final List<Account> ALL = List.of(CLIENT, BANK, CASH, SALES, VAT_COLLECTED);

  private ChartOfAccounts() {}
}
