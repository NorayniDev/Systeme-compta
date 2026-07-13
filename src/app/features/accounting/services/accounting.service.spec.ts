import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AccountingService } from './accounting.service';
import { JournalSource } from '../models/journal-entry.model';
import { ITrialBalanceLine } from '../models/trial-balance.model';
import { environment } from '../../../../environments/environment';
import { DEFAULT_PAGE_REQUEST, IPage } from '../../../core/models/pagination.model';
import { IJournalEntry } from '../models/journal-entry.model';

describe('AccountingService', () => {
  let service: AccountingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AccountingService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AccountingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch the journal', () => {
    const mockPage: IPage<IJournalEntry> = {
      content: [
        {
          id: '1',
          date: '2026-01-01',
          reference: 'FAC-2026-0001',
          accountCode: '411',
          accountName: 'Clients',
          label: 'Facture FAC-2026-0001',
          debit: 118_000,
          credit: 0,
          source: JournalSource.INVOICE,
        },
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
    };

    service.getJournal({ ...DEFAULT_PAGE_REQUEST }).subscribe((page) => {
      expect(page.content[0].accountCode).toBe('411');
    });

    const req = httpMock.expectOne(
      (request) =>
        request.url === `${environment.apiUrl}/accounting/journal` && request.method === 'GET',
    );
    req.flush(mockPage);
  });

  it('should fetch the trial balance', () => {
    const mockBalance: ITrialBalanceLine[] = [
      {
        accountCode: '411',
        accountName: 'Clients',
        totalDebit: 118_000,
        totalCredit: 0,
        balance: 118_000,
      },
    ];

    service.getTrialBalance({}).subscribe((lines) => {
      expect(lines).toEqual(mockBalance);
    });

    const req = httpMock.expectOne(
      (request) => request.url === `${environment.apiUrl}/accounting/trial-balance`,
    );
    req.flush(mockBalance);
  });
});
