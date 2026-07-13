/** Ligne de la balance générale: cumul débit/crédit d'un compte sur une période. */
export interface ITrialBalanceLine {
  accountCode: string;
  accountName: string;
  totalDebit: number;
  totalCredit: number;
  balance: number;
}

/** Filtre de période appliqué aux vues comptables. */
export interface IAccountingPeriodFilter {
  startDate?: string;
  endDate?: string;
}
