import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, QueryParams } from '../../../core/services/api.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IAccountingPeriodFilter } from '../../accounting/models/trial-balance.model';
import { IAgingReceivableLine, IQuoteFunnelLine, ISalesByClientLine } from '../models/report.model';

/**
 * Service de consultation des rapports. Comme la Comptabilité, ce module
 * est en lecture seule côté frontend — les agrégations sont calculées côté
 * backend (ou dérivées des factures/devis existants en mode démo).
 */
@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly api = inject(ApiService);

  getSalesByClient(filter: IAccountingPeriodFilter): Observable<ISalesByClientLine[]> {
    return this.api.get<ISalesByClientLine[]>(
      `${API_ENDPOINTS.REPORTS}/sales-by-client`,
      filter as unknown as QueryParams,
    );
  }

  getAgingReceivables(): Observable<IAgingReceivableLine[]> {
    return this.api.get<IAgingReceivableLine[]>(`${API_ENDPOINTS.REPORTS}/aging-receivables`);
  }

  getQuoteFunnel(): Observable<IQuoteFunnelLine[]> {
    return this.api.get<IQuoteFunnelLine[]>(`${API_ENDPOINTS.REPORTS}/quote-funnel`);
  }
}
