import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IInvoice } from '../models/invoice.model';
import { InvoiceCreateDto, InvoiceUpdateDto } from '../models/invoice.dto';

/**
 * Service métier du domaine Facture. Hérite du CRUD générique de
 * `BaseService` et ajoute les actions spécifiques au cycle de vie de la
 * facture (validation comptable, envoi au client).
 */
@Injectable({ providedIn: 'root' })
export class InvoiceService extends BaseService<IInvoice, InvoiceCreateDto, InvoiceUpdateDto> {
  protected readonly resourcePath = API_ENDPOINTS.INVOICES;

  validate(id: string): Observable<IInvoice> {
    return this.api.post<IInvoice>(`${this.resourcePath}/${id}/validate`, {});
  }

  send(id: string): Observable<IInvoice> {
    return this.api.post<IInvoice>(`${this.resourcePath}/${id}/send`, {});
  }

  downloadPdf(id: string): Observable<Blob> {
    return this.api.getBlob(`${this.resourcePath}/${id}/pdf`);
  }
}
