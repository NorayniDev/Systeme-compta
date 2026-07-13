import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, QueryParams } from '../../../core/services/api.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { ISearchFilter, IPage } from '../../../core/models/pagination.model';
import { IJournalEntry } from '../models/journal-entry.model';
import { IAccount } from '../models/account.model';
import { IAccountingPeriodFilter, ITrialBalanceLine } from '../models/trial-balance.model';

export type IJournalFilter = ISearchFilter & IAccountingPeriodFilter;

/**
 * Service de consultation des vues comptables. Contrairement aux modules
 * CRUD (Clients, Factures, ...), la comptabilité est ici exposée en lecture
 * seule côté frontend : les écritures sont générées côté backend à partir
 * des opérations métier (facturation, encaissement).
 */
@Injectable({ providedIn: 'root' })
export class AccountingService {
  private readonly api = inject(ApiService);

  getJournal(filter: IJournalFilter): Observable<IPage<IJournalEntry>> {
    return this.api.get<IPage<IJournalEntry>>(
      API_ENDPOINTS.ACCOUNTING.JOURNAL,
      filter as unknown as QueryParams,
    );
  }

  getLedger(accountCode: string, filter: IJournalFilter): Observable<IPage<IJournalEntry>> {
    return this.api.get<IPage<IJournalEntry>>(
      `${API_ENDPOINTS.ACCOUNTING.LEDGER}/${accountCode}`,
      filter as unknown as QueryParams,
    );
  }

  getTrialBalance(filter: IAccountingPeriodFilter): Observable<ITrialBalanceLine[]> {
    return this.api.get<ITrialBalanceLine[]>(
      API_ENDPOINTS.ACCOUNTING.TRIAL_BALANCE,
      filter as unknown as QueryParams,
    );
  }

  getCashbook(filter: IJournalFilter): Observable<IPage<IJournalEntry>> {
    return this.api.get<IPage<IJournalEntry>>(
      API_ENDPOINTS.ACCOUNTING.CASHBOOK,
      filter as unknown as QueryParams,
    );
  }

  getAccounts(): Observable<IAccount[]> {
    return this.api.get<IAccount[]>(`${API_ENDPOINTS.ACCOUNTING.JOURNAL}/accounts`);
  }
}
