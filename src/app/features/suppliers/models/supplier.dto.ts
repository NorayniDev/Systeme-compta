import { SupplierStatus } from './supplier.model';

/** DTO envoyé lors de la création d'un fournisseur. */
export interface SupplierCreateDto {
  name: string;
  email: string;
  phone: string;
  address: string;
  taxId: string;
}

/** DTO envoyé lors de la mise à jour d'un fournisseur. */
export interface SupplierUpdateDto extends SupplierCreateDto {
  status: SupplierStatus;
}
