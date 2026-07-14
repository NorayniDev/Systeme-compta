import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API_ENDPOINTS } from '../constants/api-endpoints.constant';
import { IPage, IPageRequest } from '../models/pagination.model';
import { INotification } from '../models/notification.model';

/**
 * Distinct de `NotificationService` (core/services/notification.service.ts), qui pilote les toasts
 * `MatSnackBar` de retour CRUD - ce service-ci parle au flux de notifications persistées côté
 * backend (cloche du layout).
 */
@Injectable({ providedIn: 'root' })
export class NotificationCenterService {
  private readonly api = inject(ApiService);
  private readonly resourcePath = API_ENDPOINTS.NOTIFICATIONS;

  search(request: IPageRequest): Observable<IPage<INotification>> {
    return this.api.get<IPage<INotification>>(this.resourcePath, {
      page: request.page,
      size: request.size,
    });
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.api.get<{ count: number }>(`${this.resourcePath}/unread-count`);
  }

  markAsRead(id: string): Observable<void> {
    return this.api.patch<void>(`${this.resourcePath}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.api.post<void>(`${this.resourcePath}/read-all`, {});
  }
}
