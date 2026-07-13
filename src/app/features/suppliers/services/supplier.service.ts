import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { ISupplier } from '../models/supplier.model';
import { SupplierCreateDto, SupplierUpdateDto } from '../models/supplier.dto';

/**
 * Service métier du domaine Fournisseur. Hérite de toutes les opérations
 * CRUD génériques de `BaseService` — symétrique de `ClientService`.
 */
@Injectable({ providedIn: 'root' })
export class SupplierService extends BaseService<ISupplier, SupplierCreateDto, SupplierUpdateDto> {
  protected readonly resourcePath = API_ENDPOINTS.SUPPLIERS;
}
