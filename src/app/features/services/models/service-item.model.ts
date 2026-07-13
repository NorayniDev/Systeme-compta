export enum ServiceItemStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

/** Unité de facturation d'une prestation. */
export enum ServiceItemUnit {
  HOUR = 'HOUR',
  DAY = 'DAY',
  MONTH = 'MONTH',
  FLAT_RATE = 'FLAT_RATE',
}

/**
 * Prestation de service facturable (catalogue), distincte d'un `IProduct`
 * physique — pas de suivi de stock.
 */
export interface IServiceItem {
  id: string;
  name: string;
  code: string;
  description: string;
  category: string;
  unitPrice: number;
  taxRate: number;
  unit: ServiceItemUnit;
  status: ServiceItemStatus;
  createdAt: string;
  updatedAt: string;
}
