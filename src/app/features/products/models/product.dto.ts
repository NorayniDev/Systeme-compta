import { ProductStatus, ProductUnit } from './product.model';

/** DTO envoyé lors de la création d'un produit. */
export interface ProductCreateDto {
  name: string;
  sku: string;
  description: string;
  category: string;
  unitPrice: number;
  taxRate: number;
  unit: ProductUnit;
  stockQuantity: number;
}

/** DTO envoyé lors de la mise à jour d'un produit. */
export interface ProductUpdateDto extends ProductCreateDto {
  status: ProductStatus;
}
