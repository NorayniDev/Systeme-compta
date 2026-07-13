export enum QuoteStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED',
  CONVERTED = 'CONVERTED',
}

export interface IQuoteLine {
  id?: string;
  description: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
  lineTotal: number;
}

export interface IQuote {
  id: string;
  number: string;
  clientId: string;
  clientName: string;
  issueDate: string;
  validUntil: string;
  lines: IQuoteLine[];
  amountExclTax: number;
  taxAmount: number;
  totalAmount: number;
  status: QuoteStatus;
  notes?: string;
  convertedInvoiceId?: string;
  createdAt: string;
  updatedAt: string;
}

/** Couleur d'accent associée à chaque statut, utilisée par les chips de statut. */
export const QUOTE_STATUS_ACCENT: Record<QuoteStatus, 'primary' | 'success' | 'warning' | 'error'> =
  {
    [QuoteStatus.DRAFT]: 'primary',
    [QuoteStatus.SENT]: 'primary',
    [QuoteStatus.ACCEPTED]: 'success',
    [QuoteStatus.REJECTED]: 'error',
    [QuoteStatus.EXPIRED]: 'error',
    [QuoteStatus.CONVERTED]: 'success',
  };
