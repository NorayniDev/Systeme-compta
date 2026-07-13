/** Ligne du rapport "Ventes par client": cumul des factures sur la période. */
export interface ISalesByClientLine {
  clientId: string;
  clientName: string;
  invoiceCount: number;
  amountExclTax: number;
  taxAmount: number;
  totalAmount: number;
}

/** Ligne du rapport "Créances échues": factures impayées et leur ancienneté. */
export interface IAgingReceivableLine {
  invoiceId: string;
  invoiceNumber: string;
  clientName: string;
  dueDate: string;
  daysOverdue: number;
  amountDue: number;
}

/** Ligne du rapport "Devis par statut": entonnoir de conversion commerciale. */
export interface IQuoteFunnelLine {
  status: string;
  count: number;
  totalAmount: number;
}
