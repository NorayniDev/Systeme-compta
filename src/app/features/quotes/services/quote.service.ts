import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IQuote } from '../models/quote.model';
import { QuoteCreateDto, QuoteUpdateDto } from '../models/quote.dto';
import { IInvoice } from '../../invoices/models/invoice.model';

/**
 * Service métier du domaine Devis. Hérite du CRUD générique de
 * `BaseService` et ajoute le cycle de vie propre au devis : envoi,
 * acceptation/refus par le client, et transformation en facture.
 */
@Injectable({ providedIn: 'root' })
export class QuoteService extends BaseService<IQuote, QuoteCreateDto, QuoteUpdateDto> {
  protected readonly resourcePath = API_ENDPOINTS.QUOTES;

  send(id: string): Observable<IQuote> {
    return this.api.post<IQuote>(`${this.resourcePath}/${id}/send`, {});
  }

  accept(id: string): Observable<IQuote> {
    return this.api.post<IQuote>(`${this.resourcePath}/${id}/accept`, {});
  }

  reject(id: string): Observable<IQuote> {
    return this.api.post<IQuote>(`${this.resourcePath}/${id}/reject`, {});
  }

  convertToInvoice(id: string): Observable<IInvoice> {
    return this.api.post<IInvoice>(`${this.resourcePath}/${id}/convert-to-invoice`, {});
  }

  downloadPdf(id: string): Observable<Blob> {
    return this.api.getBlob(`${this.resourcePath}/${id}/pdf`);
  }
}
