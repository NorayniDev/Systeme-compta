import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IPayment } from '../models/payment.model';
import { PaymentCreateDto, PaymentUpdateDto } from '../models/payment.dto';

/**
 * Service métier du domaine Paiement. Hérite du CRUD générique de
 * `BaseService` et ajoute le remboursement, seule transition d'état propre
 * au paiement une fois celui-ci complété.
 */
@Injectable({ providedIn: 'root' })
export class PaymentService extends BaseService<IPayment, PaymentCreateDto, PaymentUpdateDto> {
  protected readonly resourcePath = API_ENDPOINTS.PAYMENTS;

  refund(id: string): Observable<IPayment> {
    return this.api.post<IPayment>(`${this.resourcePath}/${id}/refund`, {});
  }

  downloadReceipt(id: string): Observable<Blob> {
    return this.api.getBlob(`${this.resourcePath}/${id}/receipt`);
  }
}
