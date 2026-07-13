import { ServiceItemStatus, ServiceItemUnit } from './service-item.model';

/** DTO envoyé lors de la création d'une prestation. */
export interface ServiceItemCreateDto {
  name: string;
  code: string;
  description: string;
  category: string;
  unitPrice: number;
  taxRate: number;
  unit: ServiceItemUnit;
}

/** DTO envoyé lors de la mise à jour d'une prestation. */
export interface ServiceItemUpdateDto extends ServiceItemCreateDto {
  status: ServiceItemStatus;
}
