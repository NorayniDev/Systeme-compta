export enum InvoiceStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT',
  PAID = 'PAID',
  PARTIALLY_PAID = 'PARTIALLY_PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED',
}

export interface IInvoiceLine {
  id?: string;
  description: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
  lineTotal: number;
}

export interface IInvoice {
  id: string;
  number: string;
  clientId: string;
  clientName: string;
  issueDate: string;
  dueDate: string;
  lines: IInvoiceLine[];
  amountExclTax: number;
  taxAmount: number;
  totalAmount: number;
  status: InvoiceStatus;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

/** Couleur d'accent associée à chaque statut, utilisée par les chips de statut. */
export const INVOICE_STATUS_ACCENT: Record<
  InvoiceStatus,
  'primary' | 'success' | 'warning' | 'error'
> = {
  [InvoiceStatus.DRAFT]: 'primary',
  [InvoiceStatus.SENT]: 'primary',
  [InvoiceStatus.PAID]: 'success',
  [InvoiceStatus.PARTIALLY_PAID]: 'warning',
  [InvoiceStatus.OVERDUE]: 'error',
  [InvoiceStatus.CANCELLED]: 'error',
};
