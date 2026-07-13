import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import {
  IDashboardKpis,
  IInvoiceStatusBreakdown,
  IRecentActivity,
  IRevenueChartPoint,
} from '../models/dashboard.model';

/**
 * Agrège les appels API nécessaires au tableau de bord.
 * Logique métier isolée du composant `Dashboard`, conformément au principe
 * "aucune logique métier dans les composants".
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = inject(ApiService);

  getKpis(): Observable<IDashboardKpis> {
    return this.api.get<IDashboardKpis>(API_ENDPOINTS.DASHBOARD.KPIS);
  }

  getRevenueChart(): Observable<IRevenueChartPoint[]> {
    return this.api.get<IRevenueChartPoint[]>(API_ENDPOINTS.DASHBOARD.REVENUE_CHART);
  }

  getInvoiceStatusBreakdown(): Observable<IInvoiceStatusBreakdown[]> {
    return this.api.get<IInvoiceStatusBreakdown[]>(
      `${API_ENDPOINTS.DASHBOARD.REVENUE_CHART}/status`,
    );
  }

  getRecentActivity(): Observable<IRecentActivity[]> {
    return this.api.get<IRecentActivity[]>(API_ENDPOINTS.DASHBOARD.RECENT_ACTIVITY);
  }
}
