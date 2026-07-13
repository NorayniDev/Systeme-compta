export enum SupplierStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export interface ISupplier {
  id: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  taxId: string;
  status: SupplierStatus;
  totalPurchased: number;
  createdAt: string;
  updatedAt: string;
}
