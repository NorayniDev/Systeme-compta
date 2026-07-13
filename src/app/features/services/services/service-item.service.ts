import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IServiceItem } from '../models/service-item.model';
import { ServiceItemCreateDto, ServiceItemUpdateDto } from '../models/service-item.dto';

/**
 * Service métier du domaine Prestation. Hérite de toutes les opérations
 * CRUD génériques de `BaseService`. Nommé `ServiceItemService` (plutôt que
 * `ServiceService`) pour éviter toute ambiguïté avec le terme générique
 * "service" d'Angular.
 */
@Injectable({ providedIn: 'root' })
export class ServiceItemService extends BaseService<
  IServiceItem,
  ServiceItemCreateDto,
  ServiceItemUpdateDto
> {
  protected readonly resourcePath = API_ENDPOINTS.SERVICES;
}
