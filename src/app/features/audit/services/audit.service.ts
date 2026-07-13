import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, QueryParams } from '../../../core/services/api.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IPage, ISearchFilter } from '../../../core/models/pagination.model';
import { IAuditLog } from '../models/audit-log.model';

/**
 * Service de consultation du journal d'audit. Lecture seule — aucune
 * opération de création/modification n'est exposée côté frontend.
 */
@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly api = inject(ApiService);

  getLogs(filter: ISearchFilter): Observable<IPage<IAuditLog>> {
    return this.api.get<IPage<IAuditLog>>(API_ENDPOINTS.AUDIT, filter as unknown as QueryParams);
  }
}
