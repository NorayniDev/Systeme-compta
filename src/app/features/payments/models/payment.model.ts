export enum PaymentMethod {
  CASH = 'CASH',
  BANK_TRANSFER = 'BANK_TRANSFER',
  MOBILE_MONEY = 'MOBILE_MONEY',
  CHECK = 'CHECK',
  CARD = 'CARD',
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
}

export interface IPayment {
  id: string;
  reference: string;
  invoiceId: string;
  invoiceNumber: string;
  clientId: string;
  clientName: string;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  paidAt: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

/** Couleur d'accent associée à chaque statut, utilisée par les chips de statut. */
export const PAYMENT_STATUS_ACCENT: Record<
  PaymentStatus,
  'primary' | 'success' | 'warning' | 'error'
> = {
  [PaymentStatus.PENDING]: 'warning',
  [PaymentStatus.COMPLETED]: 'success',
  [PaymentStatus.FAILED]: 'error',
  [PaymentStatus.REFUNDED]: 'primary',
};

/** Icône associée à chaque moyen de paiement. */
export const PAYMENT_METHOD_ICON: Record<PaymentMethod, string> = {
  [PaymentMethod.CASH]: 'payments',
  [PaymentMethod.BANK_TRANSFER]: 'account_balance',
  [PaymentMethod.MOBILE_MONEY]: 'phone_iphone',
  [PaymentMethod.CHECK]: 'receipt_long',
  [PaymentMethod.CARD]: 'credit_card',
};
