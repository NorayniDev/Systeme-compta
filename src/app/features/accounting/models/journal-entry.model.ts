/** Origine métier d'une écriture comptable, pour traçabilité. */
export enum JournalSource {
  INVOICE = 'INVOICE',
  PAYMENT = 'PAYMENT',
  MANUAL = 'MANUAL',
}

/**
 * Écriture comptable élémentaire (une ligne de débit OU de crédit).
 * Une opération métier (ex: émission d'une facture) génère plusieurs
 * `IJournalEntry` partageant la même `reference`, conformément au principe
 * de la partie double.
 */
export interface IJournalEntry {
  id: string;
  date: string;
  reference: string;
  accountCode: string;
  accountName: string;
  label: string;
  debit: number;
  credit: number;
  source: JournalSource;
}
