import { QuoteStatus } from './quote.model';

export interface QuoteLineDto {
  description: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
}

export interface QuoteCreateDto {
  clientId: string;
  issueDate: string;
  validUntil: string;
  lines: QuoteLineDto[];
  notes?: string;
}

export interface QuoteUpdateDto extends QuoteCreateDto {
  status: QuoteStatus;
}
