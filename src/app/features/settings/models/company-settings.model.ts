/** Paramètres généraux de l'entreprise, utilisés notamment sur les documents édités (factures, devis). */
export interface ICompanySettings {
  companyName: string;
  address: string;
  taxId: string;
  currency: string;
  defaultTaxRate: number;
  invoicePrefix: string;
  quotePrefix: string;
}
