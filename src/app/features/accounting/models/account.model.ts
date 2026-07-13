/** Classe d'un compte du plan comptable (SYSCOHADA/PCG simplifié). */
export enum AccountType {
  ASSET = 'ASSET',
  LIABILITY = 'LIABILITY',
  EQUITY = 'EQUITY',
  REVENUE = 'REVENUE',
  EXPENSE = 'EXPENSE',
}

export interface IAccount {
  code: string;
  name: string;
  type: AccountType;
}
