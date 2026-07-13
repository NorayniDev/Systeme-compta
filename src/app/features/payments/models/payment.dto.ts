import { PaymentMethod, PaymentStatus } from './payment.model';

/** DTO envoyé lors de l'enregistrement d'un nouvel encaissement. */
export interface PaymentCreateDto {
  invoiceId: string;
  amount: number;
  method: PaymentMethod;
  paidAt: string;
  notes?: string;
}

/** DTO envoyé lors de la mise à jour d'un paiement (correction administrative). */
export interface PaymentUpdateDto extends PaymentCreateDto {
  status: PaymentStatus;
}
