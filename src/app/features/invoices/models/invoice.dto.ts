import { InvoiceStatus } from './invoice.model';

export interface InvoiceLineDto {
  description: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
}

export interface InvoiceCreateDto {
  clientId: string;
  issueDate: string;
  dueDate: string;
  lines: InvoiceLineDto[];
  notes?: string;
}

export interface InvoiceUpdateDto extends InvoiceCreateDto {
  status: InvoiceStatus;
}
