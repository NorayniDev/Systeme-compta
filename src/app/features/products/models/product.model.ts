export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

/** Unité de mesure utilisée pour la quantité vendue/en stock. */
export enum ProductUnit {
  PIECE = 'PIECE',
  HOUR = 'HOUR',
  KG = 'KG',
  LITER = 'LITER',
  METER = 'METER',
  BOX = 'BOX',
}

export interface IProduct {
  id: string;
  name: string;
  sku: string;
  description: string;
  category: string;
  unitPrice: number;
  taxRate: number;
  unit: ProductUnit;
  stockQuantity: number;
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
}
